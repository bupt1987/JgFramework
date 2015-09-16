package com.zhaidaosi.game.jgframework.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@SuppressWarnings("resource")
public class BaseFile {

    private static final Logger log = LoggerFactory.getLogger(BaseFile.class);


    public static Set<Class<?>> getClasses(String pack, boolean recursive) {
        return getClasses(pack, "", recursive);
    }

    /**
     * 从包package中获取所有的Class
     *
     * @param pack String
     * @param suffix String
     * @param recursive 是迭代
     * @return String
     */
    public static Set<Class<?>> getClasses(String pack, String suffix, boolean recursive) {
        // 第一个class类的集合
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        // 获取包的名字 并进行替换
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, suffix, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    JarFile jar;
                    try {
                        String jarPath = url.getPath();
                        if (jarPath.indexOf("file:") == 0) {
                            jarPath = jarPath.substring(5);
                        }
                        int index = jarPath.lastIndexOf(".jar");
                        jarPath = jarPath.substring(0, index) + ".jar";
                        // 获取jar
                        jar = new JarFile(jarPath);
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            // 添加到classes
                                            classes.add(Class.forName(packageName + '.' + className));
                                        } catch (ClassNotFoundException e) {
                                            log.error("添加用户自定义视图类错误 找不到此类的.class文件", e);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        log.error("在扫描用户定义视图时从jar包获取文件出错", e);
                    }
                }
            }
        } catch (IOException e) {
        }

        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final String suffix, final boolean recursive, Set<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            // log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(suffix + ".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), suffix, recursive, classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    // classes.add(Class.forName(packageName + '.' +
                    // className));
                    // 经过回复同学的提醒，这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    log.error("添加用户自定义视图类错误 找不到此类的.class文件", e);
                }
            }
        }
    }

    public static List<String> fileList(String path) {
        return fileList(path, false);
    }

    public static List<String> fileList(String path, boolean needDir) {
        List<String> list = new ArrayList<String>();
        File file = new File(path);
        if (file.isDirectory()) {
            String[] filelist = file.list();
            for (int i = 0; i < filelist.length; i++) {
                file = new File(path + "/" + filelist[i]);
                if (!file.isDirectory()) {
                    list.add(file.getName());
                } else {
                    if (needDir) {
                        list = fileList(list, path, file.getName());
                    }
                }
            }
        }
        return list;
    }

    private static List<String> fileList(List<String> list, String path, String dir) {
        File file = new File(path + "/" + dir);
        if (file.isDirectory()) {
            String[] filelist = file.list();
            for (int i = 0; i < filelist.length; i++) {
                file = new File(path + "/" + dir + "/" + filelist[i]);
                if (!file.isDirectory()) {
                    list.add(dir + "/" + file.getName());
                } else {
                    list = fileList(list, path, dir + "/" + file.getName());
                }
            }
        }
        return list;
    }

    public static String readResourceByLines(String resourcePath) {
        // 返回读取指定资源的输入流
        InputStream is = BaseFile.class.getClassLoader().getResourceAsStream(resourcePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer("");
        try {
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                buffer.append(tempString);
                buffer.append("\n");
            }
        } catch (IOException e) {
            log.error("读取文件失败", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return buffer.toString();
    }

    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public static String readFileByLines(String fileName) {
        File file = new File(fileName);
        return readFileByLines(file);
    }

    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public static String readFileByLines(File file) {
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer("");
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                buffer.append(tempString);
                buffer.append("\n");
            }
            reader.close();
        } catch (IOException e) {
            log.error("读取文件失败", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return buffer.toString();
    }

    public static String readFileLastByLine(String filePath, int lastStart, int limit) {
        File file = new File(filePath);
        String buffer = "";
        if (!file.exists()) {
            return buffer;
        }
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
            long len = raf.length();
            int line = 0;
            if (len != 0L) {
                long pos = len - 1;
                while (pos > 0) {
                    raf.seek(pos);
                    if (raf.readByte() == '\n') {
                        if (lastStart <= line && line < lastStart + limit) {
                            String temp = "";
                            // Windows下有乱码问题
                            String tempLine = raf.readLine();
                            if (tempLine == null) {
                                pos--;
                                continue;
                            }
                            temp += tempLine;
                            temp += "\n";
                            buffer += temp;
                            pos--;
                        }
                        line++;
                    } else {
                        pos--;
                    }
                    if (line >= lastStart + limit) {
                        break;
                    }
                }
            }
            raf.close();
        } catch (IOException e) {
            log.error("读取文件失败", e);
        }
        return buffer;
    }

    /**
     * 判断文件的编码格式
     *
     * @param fileName
     *            :file
     * @return 文件编码格式
     * @throws Exception
     */
    public static String codeString(String fileName) throws Exception {
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));
        int p = (bin.read() << 8) + bin.read();
        String code = null;
        // 其中的 0xefbb、0xfffe、0xfeff、0x5c75这些都是这个文件的前面两个字节的16进制数
        switch (p) {
            case 0xefbb:
                code = "UTF-8";
                break;
            case 0xfffe:
                code = "Unicode";
                break;
            case 0xfeff:
                code = "UTF-16BE";
                break;
            case 0x5c75:
                code = "ANSI|ASCII";
                break;
            default:
                code = "GBK";
        }

        return code;
    }

}