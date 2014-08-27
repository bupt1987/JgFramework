package com.zhaidaosi.game.jgframework.common.spring;

import org.hibernate.HibernateException;
import org.hibernate.cfg.AnnotationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.util.CollectionUtils;

import javax.persistence.Entity;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 该类扩展了Spring的AnnotationSessionFactoryBean类，增加了对AnnotatedClass的批量导入功能。
 * 通过使用setAnnotatedClassesLocations(String[])方法（其中的参数，是导入文件的路径数组），
 * 添加所有需要导入的AnnotatedClass，支持Spring的ResourceLoader对资源的加载方式。
 * 通过使用setExcludedClasses(String[])方法（其中参数，是待去除的类的全路径名），
 * 来去除在上述包路径中不需要导入的AnnotatedClass。
 */
public class AnnotationSessionFactoryBeanEx extends
        AnnotationSessionFactoryBean {
    private static final Logger logger = LoggerFactory
            .getLogger(AnnotationSessionFactoryBeanEx.class);

    /**
     * The locations of the hibernate enity class files. They are often some of
     * the string with Sping-style resource. A ".class" subfix can make the
     * scaning more precise.
     * example:
     * classpath*:com/systop/** /model/*.class
     */
    private String[] annotatedClassesLocations;

    /**
     * Which classes are not included in the session. They are some of the
     * regular expression.
     */
    private Set<String> excludedClasseses = new HashSet<String>(0);

    /**
     * @param annotatedClassesLocations
     *            the annotatedClassesLocations to set
     */
    public void setAnnotatedClassesLocations(String[] annotatedClassesLocations) {
        this.annotatedClassesLocations = annotatedClassesLocations;
    }

    /**
     * @see AnnotationSessionFactoryBean#postProcessAnnotationConfiguration(org.hibernate.cfg.AnnotationConfiguration)
     */
    @Override
    protected void postProcessAnnotationConfiguration(
            AnnotationConfiguration config) throws HibernateException {
        Set<Class<?>> annClasses = scanAnnotatedClasses(); // Scan enity
        // classes.
        // Add entity classes to the configuration.
        if (!CollectionUtils.isEmpty(annClasses)) {
            for (Class<?> annClass : annClasses) {
                config.addAnnotatedClass(annClass);
            }
        }
    }

    /**
     * Scan annotated hibernate classes in the locations.
     *
     * @return Set of the annotated classes, if no matched class, return empty
     *         Set.
     */
    private Set<Class<?>> scanAnnotatedClasses() {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(
                resourcePatternResolver);
        Set<Class<?>> annotatedClasses = new HashSet<Class<?>>();
        if (annotatedClassesLocations != null) {
            try {
                for (String annClassesLocation : annotatedClassesLocations) {
                    // Resolve the resources
                    Resource[] resources = resourcePatternResolver
                            .getResources(annClassesLocation);
                    for (Resource resource : resources) {
                        MetadataReader metadataReader = metadataReaderFactory
                                .getMetadataReader(resource);
                        String className = metadataReader.getClassMetadata()
                                .getClassName();
                        // If the class is hibernate enity class, and it does
                        // not match the excluded class patterns.
                        if (isEntityClass(metadataReader)
                                && !isExcludedClass(className)) {
                            Class<?> clazz = Class.forName(className);
                            annotatedClasses.add(clazz);
                            logger.debug("A entity class has been found. ({})",
                                    clazz.getName());
                        }
                    }

                }
            } catch (IOException e) {
                logger.error("I/O failure during classpath scanning, ({})", e
                        .getMessage());
                throw new HibernateException(e);
            } catch (ClassNotFoundException e) {
                logger.error("Class not found, ({})", e.getMessage());
                throw new HibernateException(e);
            } catch (LinkageError e) {
                logger.error("LinkageError ({})", e.getMessage());
                throw new HibernateException(e);
            }
        }

        return annotatedClasses;
    }

    /**
     * @return True if the given MetadataReader shows that the class is
     *         annotated by <code>javax.persistence.Enity</code>
     */
    private boolean isEntityClass(MetadataReader metadataReader) {
        Set<String> annTypes = metadataReader.getAnnotationMetadata()
                .getAnnotationTypes();
        if (CollectionUtils.isEmpty(annTypes)) {
            return false;
        }

        return annTypes.contains(Entity.class.getName());
    }

    /**
     *
     * @return True if the given class name match the excluded class patterns.
     */
    private boolean isExcludedClass(String className) {
        return excludedClasseses.contains(className);
    }

    /**
     * @param exculdePatterns
     *            the exculdePatterns to set
     */
    public void setExcludedClasses(String[] excludedClasses) {
        for (String cls : excludedClasses) {
            this.excludedClasseses.add(cls);
        }
    }

}