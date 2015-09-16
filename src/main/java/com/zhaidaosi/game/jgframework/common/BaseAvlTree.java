package com.zhaidaosi.game.jgframework.common;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Random;

/**
 * 平衡二叉搜索（排序）树
 */
public class BaseAvlTree<E extends Comparable<E>> extends AbstractSet<E> {
    private Entry<E> root;//根节点
    private int size;//树节点个数

    private static class Entry<E> {
        E elem;//数据域
        Entry<E> parent;//父节点
        Entry<E> left;//左节点
        Entry<E> right;//右节点
        /*
         * 树的平衡因子，等号表示左子树和右子树有同样的高度。如果L，左子树比右子树高1。如果为R，则意味着右
         * 子树比左高1。刚创建时是平衡的，所以为=
         *             50
         *             /R\
         *            20  80
         *            /L  /R\
         *           10  70 100
         *           =   =  /=\
         *                 92 103
         *                 =  =
         */
        char balanceFactor = '=';

        //构造函数只有两个参数，左右节点是调用add方法时设置
        public Entry(E elem, Entry<E> parent) {
            this.elem = elem;
            this.parent = parent;
        }
    }

    private class TreeIterator implements Iterator<E> {

        private Entry<E> lastReturned = null;
        private Entry<E> next;

        TreeIterator() {

            next = root;
            if (next != null)
                while (next.left != null)
                    next = next.left;

        }

        public boolean hasNext() {

            return next != null;

        }

        public E next() {

            lastReturned = next;
            next = successor(next);
            return lastReturned.elem;

        }

        public void remove() {

            if (lastReturned != null && lastReturned.left != null)
                next = lastReturned;
            deleteEntry(lastReturned);
            lastReturned = null;

        }
    }

    /**
     * 左旋转：结果就是将p移到它的左子节点的位置，而p的右子节点被移到该元素原来位置
     * @param p 旋转元素
     */
    private void rotateLeft(Entry<E> p) {
        /*
		* 围绕50左旋转:
		*p → 50                 90
		*     \                 /\
		* r → 90      →       50 100
		*      \
		*     100
		* 
		* 围绕80左旋转:r的左子树变成p的右子树。因为位于r的左子树上的任意一个元素值大于p且小于r，所以r的左子
		* 树可以成为p的右子树这是没有问题的。其实上面也发生了同样的现象，只是r的左子树为空罢了
		*  p → 80                  90
		*      /\                  /\ 
		*     60 90 ← r     →     80 120
		*        /\               /\   /
		*      85 120           60 85 100
		*          /
		*         100
		* 
		* 围绕80在更大范围内旋转：元素不是围绕树的根旋转。旋转前后的树都是二叉搜索树。且被旋转元素80上的所
		* 有元素在旋转中不移动（50、30、20、40这四个元素还是原来位置）
		*       50                         50
		*       / \                        / \
		*     30   80 ← p                 30  90
		*     /\   /\                     /\  / \
		*   20 40 60 90 ← r      →      20 40 80 120
		*            /\                       /\   /
		*           85 120                  60 85 100
		*              /
		*             100
		* 
		* 节点数据结构:
		*  +------------------+
		*  | elem | p | l | r |
		*  +------------------+
		*  
		*    +------------------+
		*    | 50 |NULL|NULL| r |
		*    +------------------+
		*                 ↖⑥ ↘④
		*         +---------------+
		*         |80| p | l  | r |   ← p 
		*         +---------------+
		*         ↗       ↙     ↖③ ↘①      
		*  +----------------+ +---------------+
		*  |60| p |NULL|NULL| |90| p |  l | r |   ← r
		*  +----------------+ +---------------+
		*                     ↗②       ↙⑤    ↖↘ 
		*              +----------------+ +---------------+
		*              |85| p |NULL|NULL| |90| p | l |NULL|
		*              +----------------+ +---------------+
		*                                  ↗      ↙  
		*                           +-----------------+
		*                           |100| p |NULL|NULL|
		*                           +-----------------+
		*/

        Entry<E> r = p.right;//旋转元素的右子节点
        //把旋转元素的右子节点的左子节点接到旋转元素的右子树
        p.right = r.left;//①
        //如果旋转元素的右子节点存在左子节点的话，同时修改该节点的父节指针指向
        if (r.left != null) {
            //把旋转元素的右子节点的左子节点的父设置成旋转元素
            r.left.parent = p;//②
        }
        //将旋转元素的右子节点的父设置成旋转元素的父，这里有可以p为根节点，那么旋转后p就成根节点
        r.parent = p.parent;//③

        //如果旋转元素为根元素，则旋转后，旋转元素的右子节点r将成为根节点
        if (p.parent == null) {
            root = r;
        }//否则p不是根节点，如果p是他父节点的左节点时
        else if (p.parent.left == p) {
            //让p的父节点的左指针指向r
            p.parent.left = r;
        }//否则如果p是他父节点的右节点时
        else {
            //让p的父节点的右指针指向r
            p.parent.right = r;//④
        }
        //让旋转元素的左节点指向旋转元素p
        r.left = p;//⑤
        //让旋转元素的父节点指向旋转元素右节点r
        p.parent = r;//⑥
    }

    /**
     * 右旋转：结果就是将p移到它的右子节点的位置，而p的左子节点被移到该元素原来位置
     * 与左旋转是完全对称的，将左旋转中的lef与rigth互换即可得到，这里就不详细解释了
     * @param p 旋转元素
     */
    private void rotateRight(Entry<E> p) {

		/*
		* 围绕100右旋转:
		*  p → 100              90
		*       /               /\
		* l → 90      →       50 100
		*     /
		*    50
		* 
		* 围绕80右旋转:l的右子树变成p的左子树。因为位于l的右子树上的任意一个元素值小于p且小于l，所以l的右
		* 子树可以成为p的左子树这是没有问题的。其实上面也发生了同样的现象，只是l的右子树为空罢了
		* 
		*  p → 80                  60
		*      /\                  /\ 
		* l → 60 90         →     50 80
		*     /\                   \  /\
		*    50 70                55 70 90   
		*     \   
		*     55
		*/

        Entry<E> l = p.left;
        p.left = l.right;
        if (l.right != null) {
            l.right.parent = p;
        }
        l.parent = p.parent;

        if (p.parent == null) {
            root = l;
        } else if (p.parent.right == p) {
            p.parent.right = l;
        } else {
            p.parent.left = l;
        }
        l.right = p;
        p.parent = l;
    }

    /**
     * BaseAVLTree类的add方法类似于BinSerrchTree类的add方法，但是沿着根向下前进到插入点时，需记录这样一个被插
     * 入Entry对象最近的祖先：该祖先的平衡因子balanceFactor值是L或R(即不歼)，且让ancestor指向这个祖先节
     * 点，该祖先节有什么用呢，从ancestor的子开始到新增节点路径上的所有祖先节点都是平衡，这些祖先节点会因为
     * 新增节点而变得不平衡了，需要重新调整平衡因子，这个分界点在调整平衡因子时非常有用
     *
     * @param elem 要新增元素的数据域
     * @return boolean
     */
    public boolean add(E elem) {
        //如果树为空，则直接插入
        if (root == null) {
            root = new Entry<E>(elem, null);
            size++;
            return true;
        } else {
            Entry<E> tmp = root;//新增节点的父节点，从根节点下面开始找插入点
            Entry<E> ancestor = null;//平衡因子不为 = 的最近祖先节点
            int comp; //比较结果
            while (true) {//死循环，直接找到插入点退出循环
                comp = elem.compareTo(tmp.elem);
                //如果已存在该元素，则直接返回失败
                if (comp == 0) {
                    return false;
                }

                //记录不平衡的祖先节点
                if (tmp.balanceFactor != '=') {
                    //如果哪个祖先节点不平衡，则记录，当循环完后，ancestor指向的就是最近一个
                    //不平衡的祖先节点
                    ancestor = tmp;
                }

                //如果小于当前比较节点，则在其左边插入
                if (comp < 0) {

                    //如果左子树不为空，继续循环在左边找插入点
                    if (tmp.left != null) {
                        tmp = tmp.left;
                    } else {//否则插入
                        tmp.left = new Entry<E>(elem, tmp);
                        //插入后要进行平衡调整
                        fixAfterInsertion(ancestor, tmp.left);
                        size++;
                        return true;
                    }
                } else {//在右边插入

                    //如果右子树不为空，继续循环在右边找插入点
                    if (tmp.right != null) {
                        tmp = tmp.right;
                    } else {//否则插入
                        tmp.right = new Entry<E>(elem, tmp);
                        //插入后要进行平衡调整
                        fixAfterInsertion(ancestor, tmp.right);
                        size++;
                        return true;
                    }
                }

            }
        }
    }

    /**
     * 当新增节点后，会改变某些节点的平衡因子，所以添加节点后需重新调整平衡因子
     *
     * 根据前人们的分析与研究，可分为6种情况
     *
     * @param ancestor 新增元素的最近一个不平衡的祖先节点
     * @param inserted 新增元素
     */
    protected void fixAfterInsertion(Entry<E> ancestor, Entry<E> inserted) {
        E elem = inserted.elem;

        if (ancestor == null) {
			/*
			 * 情况1：ancestor的值为null，即被插入Entry对象的每个祖先的平衡因子都是 =，此时新增节点后
			 * ，树的高度不会发生变化，所以不需要旋转，我们要作的就是调整从根节点到插入节点的路径上的所有
			 * 节点的平衡因子罢了
			 * 
			 *                       新增55后调整
			 *             50            →           50
			 *             /=\                       /R\
			 *           25   70                   25   70
			 *          /=\   /=\                 /=\   /L\
			 *         15 30 60 90               15 30 60 90
			 *         =  =  =   =               =  =  /L =
			 *                                        55
			 *                                        =
			 */
            //根节点的平衡因子我们单独拿出来调整，因为adjustPath的参数好比是一个开区间，它不包括两头参数
            //本身，而是从nserted.paraent开始到to的子节点为止，所以需单独调整，其他分支也一样
            if (elem.compareTo(root.elem) < 0) {
                root.balanceFactor = 'L';
            } else {
                root.balanceFactor = 'R';
            }
            //再调用adjustPath方法调整新增节点的父节点到root的某子节点路径上的所有祖先节点的
            //平衡因子
            adjustPath(root, inserted);
        } else if ((ancestor.balanceFactor == 'L' && elem.compareTo(ancestor.elem) > 0)
                || (ancestor.balanceFactor == 'R' && elem.compareTo(ancestor.elem) < 0)) {
			/*
			 * 情况2：
			 * ancestor.balanceFactor的值为 L，且在ancestor对象的右子树插入，或ancestor.balanceFac
			 * tor的值为 R，且在ancestor对象的左子树插入，这两插入方式都不会引起树的高度发生变化，所以我们
			 * 也不需要旋转，直接调整平衡因子即可
			 *                      新增55后调整
			 *  ancestor → 50           →              50
			 *            /L\                         /=\
			 *          25   70                     25   70
			 *         /R\   /=\                   /R\   /L\
			 *        15 30 60 90                 15 30 60 90
			 *           /L                          /L /L
			 *          28                          28 55
			 *                      新增28后调整
			 *  ancestor → 50            →             50
			 *            /R\                         /=\
			 *          25   70                     25   70
			 *         /=\   /L\                   /R\   /L\
			 *        15 30 60 90                 15 30 60 90
			 *              /L                       /L /L
			 *             55                       28 55
			 */
            //先设置ancestor的平衡因子为 平衡
            ancestor.balanceFactor = '=';
            //然后按照一般策略对inserted与ancestor间的元素进行调整
            adjustPath(ancestor, inserted);
        } else if (ancestor.balanceFactor == 'R'
                && elem.compareTo(ancestor.right.elem) > 0) {
			/*
			 * 情况3：
			 * ancestor.balanceFactor的值为 R，并且被插入的Entry位于ancestor的右子树的右子树上， 此
			 * 种情况下会引起树的不平衡，所以先需绕ancestor进行旋转，再进行平衡因子的调整
			 *
			 * 新增93                          先调整因子再绕70左旋
			 *   →         50                    →           50
			 *            /R\                                /R\
			 *          25   70  ← ancestor                25  90
			 *         /L    /R\                          /L   /=\
			 *        15    60 90                        15  70   98
			 *        =     =  /=\                       =  /=\   /L
			 *                80  98                       60 80 93
			 *                =   /=                       =  =  =
			 *                   93
			 *                   =
			 */
            ancestor.balanceFactor = '=';
            //围绕ancestor执行一次左旋
            rotateLeft(ancestor);
            //再调整ancestor.paraent（90）到新增节点路径上祖先节点平衡
            adjustPath(ancestor.parent, inserted);
        } else if (ancestor.balanceFactor == 'L'
                && elem.compareTo(ancestor.left.elem) < 0) {
			/*
			 * 情况4：
			 * ancestor.balanceFactor的值为 L，并且被插入的Entry位于ancestor的左子树的左子树上，
			 * 此种情况下会引起树的不平衡，所以先需绕ancestor进行旋转，再进行平衡因子的调整
			 * 
			 * 新增13                         先调整因子再绕50右旋
			 *   →         50 ← ancestor        →            20
			 *            /L\                                /=\
			 *          20   70                            10   50
			 *         /=\   /=\                          /R\   /=\
			 *        10 30 60 90                        5  15 30  70
			 *       /=\ /=\=  =                         = /L /=\  /=\
			 *      5 15 25 35                            13 25 35 60 90
			 *      = /= = =                              =  =  =  =  = 
			 *       13         
			 *       =        
			 */
            ancestor.balanceFactor = '=';
            //围绕ancestor执行一次右旋
            rotateRight(ancestor);
            //再调整ancestor.paraent（20）到新增节点路径上祖先节点平衡
            adjustPath(ancestor.parent, inserted);
        } else if (ancestor.balanceFactor == 'L'
                && elem.compareTo(ancestor.left.elem) > 0) {
			/*
			 * 情况5：
			 * ancestor.balanceFactor的值为 L，并且被插入的Entry位于ancestor的左子树的右子树上。此
			 * 种情况也会导致树不平衡，此种与第6种一样都需要执行两次旋转（执行一次绕ancestor的左子节点左
			 * 旋，接着执行一次绕ancestor执行一次右旋）后，树才平衡，最后还需调用 左-右旋 专有方法进行平衡
			 * 因子的调整
			 */
            rotateLeft(ancestor.left);
            rotateRight(ancestor);
            //旋转后调用 左-右旋 专有方法进行平衡因子的调整
            adjustLeftRigth(ancestor, inserted);
        } else if (ancestor.balanceFactor == 'R'
                && elem.compareTo(ancestor.right.elem) < 0) {

			/*
			 * 情况6：
			 * ancestor.balanceFactor的值为 R，并且被插入的Entry位于ancestor的右子树的 左子树上，此
			 * 种情况也会导致树不平衡，此种与第5种一样都需要执行两次旋转（执行一次绕ancestor的右子节点右旋
			 * ，接着执行一次绕ancestor执行一次左旋）后，树才平衡，最后还需调用 右-左旋 专有方法进行平衡因
			 * 子的调整
			 */
            rotateRight(ancestor.right);
            rotateLeft(ancestor);
            //旋转后调用 右-左旋 专有方法进行平衡因子的调整
            adjustRigthLeft(ancestor, inserted);
        }
    }


    /**
     * 调整指定路径上的节点的平衡因子
     *
     * 注，指定的路径上的所有节点一定是平衡的，因此如果被插入元素小于某个祖先节点，
     * 则这个祖先节点新的平衡因子是 L，反之为 R。
     *
     * @param inserted 从哪里元素开始向上调整，但不包括该，即从父开始）
     * @param to 直到哪个元素结束,但不包括该元素，一般传进来的为ancestor
     */
    protected void adjustPath(Entry<E> to, Entry<E> inserted) {
        E elem = inserted.elem;
        Entry<E> tmp = inserted.parent;
        //从新增节点的父节点开始向上回溯调整，直到结尾节点to止
        while (tmp != to) {
			/*
			 * 插入30，则在25右边插入，这样父节点平衡会被打破，右子树就会比左子树高1，所以平衡因子为R；祖
			 * 先节点50的平衡因子也被打破，因为在50的左子树上插入的，所以对50来说，左子树会比右子树高1，所
			 * 以其平衡因子为L
			 *    50                      50
			 *    /=\       插入30        /L\
			 *   25  70       →         25  70
			 *   =   =                   R\  =
			 *                            30
			 *                            = 
			 */
            //如果新增元素比祖先节点小，则是在祖先节点的左边插入，则自然该祖先的左比右子树会高1
            if (elem.compareTo(tmp.elem) < 0) {

                tmp.balanceFactor = 'L';
            } else {
                //否则会插到右边，那么祖先节点的右就会比左子树高1
                tmp.balanceFactor = 'R';
            }
            tmp = tmp.parent;//再调整祖先的祖先
        }
    }

    /**
     * 进行 左-右旋转 后平衡因子调整
     * 分三种情况
     * @param ancestor
     * @param inserted
     */
    protected void adjustLeftRigth(Entry<E> ancestor, Entry<E> inserted) {
        E elem = inserted.elem;
        if (ancestor.parent == inserted) {
			/*
			 * 第1种，新增的节点在旋转完成后为ancestor父节点情况：
			 * 
			 * 新增40                          绕30左旋                绕50右旋
			 *   →      50 ← ancestor         →         50         →
			 *          /L                              /L         
			 *         30                              40  
			 *         =\                             /=
			 *          40                           30
			 *          =                            =
			 *          
			 *                    调整平衡因子
			 *          40            →            40
			 *          /=\                        /=\
			 *         30 50                      30 50
			 *         =  L                       =   =
			 *         
			 * 注，这里的 左-右旋 是在fixAfterInsertion方法中的第5种情况中完成的，在这里只是平衡因子的
			 * 调整，图是为了好说明问题而放在这个方法中的，下面的两个分支也是一样      
			 */
            ancestor.balanceFactor = '=';
        } else if (elem.compareTo(ancestor.parent.elem) < 0) {
			/*
			 * 第2种，新增的节点在旋转完成后为不为ancestor父节点，且新增节点比旋转后ancestor的父节点要小
			 * 的情况
			 * 
			 * 由于插入元素(35)比旋转后ancestor(50)的父节点(40)要小， 所以新增节点会在其左子树中
			 * 
			 * 新增35                         绕20左旋
			 *   →      50 ← ancestor        →                 50
			 *          /L\                                    /L\
			 *        20   90                                40   90
			 *       /=\   /=\                              /=\   /=\
			 *     10  40 70  100                          20 45 70 100
			 *    /=\  /=\=   =                            /=\    
			 *   5  15 30 45                              10  30   
			 *   =  =  =\ =                              /=\   =\
			 *          35                              5  15   35
			 *          =                               =  =    =
			 *          
			 *  绕50右旋                      调整平衡因子
			 *     →        40                →                40
			 *              /=\                                /=\
			 *             20  50                            20   50
			 *            /=\  /L\                          /=\   /R\
			 *          10 30 45 90                        10 30 45  90
			 *         /=\  =\   /=\                      /=\  R\    /=\
			 *        5  15  35 70 100                   5  15  35  70  100
			 *        =  =   =  =  =                     =  =   =   =   =
			 *          
			 */
            ancestor.balanceFactor = 'R';
            //调整ancestor兄弟节点到插入点路径上节点平衡因子
            adjustPath(ancestor.parent.left, inserted);
        } else {
			/*
			 * 第2种，新增的节点在旋转完成后为不为ancestor父节点，且新增节点比旋转后ancestor的父节点要大的
			 * 情况
			 * 
			 * 由于插入元素(42)比旋转后ancestor(50)的父节点(40)要大，所以新增节点会在其右子树中
			 * 
			 * 新增42                           绕20左旋
			 *   →      50 ← ancestor          →               50
			 *          /L\                                    /L\
			 *        20   90                                40   90
			 *       /=\   /=\                              /=\    /=\
			 *     10  40 70  100                          20  45 70 100
			 *    /=\  /=\=   =                           /=\  /=
			 *   5  15 30 45                             10 30 42  
			 *   =  =  =  /=                             /=\=  =
			 *           42                             5  15   
			 *           =                              =  =    
			 *          
			 * 绕50右旋                        调整平衡因子
			 *   →          40                 →               40
			 *              /=\                                /=\
			 *             20  50                            20   50
			 *            /=\  /L\                          /L\   /=\
			 *          10 30 45 90                        10 30 45  90
			 *         /=\   /=  /=\                      /=\    /L  /=\
			 *        5  15 42  70 100                    5 15  42  70 100
			 *        =  =  =   =  =                      =  =  =   =  =
			 *          
			 */
            ancestor.parent.left.balanceFactor = 'L';

            ancestor.balanceFactor = '=';
            //调整ancestor节点到插入点路径上节点平衡因子
            adjustPath(ancestor, inserted);
        }
    }

    /**
     * 进行 右-左旋转 后平衡因子调整
     *
     * 与adjustLeftRigth方法一样，也有三种情况，这两个方法是对称的
     * @param ancestor
     * @param inserted
     */
    protected void adjustRigthLeft(Entry<E> ancestor, Entry<E> inserted) {
        E elem = inserted.elem;
        if (ancestor.parent == inserted) {
			/*
			 * 第1种，新增的节点在旋转完成后为ancestor父节点情况： 
			 * 
			 * 新增40                          绕50右旋                绕30左旋 
			 *   →       30 ← ancestor        →        30          →
			 *           R\                            R\         
			 *            50                            40  
			 *           /=                              =\
			 *          40                                50
			 *          =                                 =
			 *          
			 *          40         调整平衡因子         40
			 *          /=\           →            /=\
			 *         30 50                      30  50
			 *         R  =                       =   =
			 *         
			 * 注，这里的 右-左旋 是在fixAfterInsertion方法中的第6种情况中完成的，这里只是 平衡因子的调
			 * 整，图是为了好说明问题而放在这个方法中的，下面的两个分支也是一样      
			 */
            ancestor.balanceFactor = '=';
        } else if (elem.compareTo(ancestor.parent.elem) > 0) {
			/*
			 * 第2种，新增的节点在旋转完成后为不为ancestor父节点，且新增节点比旋转后
			 * ancestor的父节点要大的情况
			 * 
			 * 由于插入元素(73)比旋转后ancestor(50)的父节点(70)要大， 所以新增节点会
			 * 在其右子树中
			 * 
			 * 新增73                          绕90右旋
			 *   →       50 ← ancestor       →                  50
			 *          /R\                                    /R\
			 *        20   90                                20   70
			 *       /=\   /=\                              /=\   /=\
			 *     10  40 70  95                          10  40 65 90
			 *     =   = /=\  /=\                         =   =  =  /=\ 
			 *          65 75 93 97                                75  95
			 *          =  /= =  =                                 /=  /=\
			 *            73                                      73  93 97
			 *            =    
			 *                          
			 * 绕50左旋                       调整平衡因子
			 *   →          70                →                70
			 *              /=\                                /=\
			 *            50   90                            50   90
			 *           /R\   /=\                          /L\   /=\
			 *          20 65 75  95                       20 65 75  95
			 *         /=\ =  /=  /=\                     /=\ =  /L  /=\
			 *        10  40 73  93 97                   10  40 73  93 97
			 *        =   =   =   =  =                   =   =  =   =   =
			 *          
			 */
            ancestor.balanceFactor = 'L';
            adjustPath(ancestor.parent.right, inserted);
        } else {
			/*
			 * 第2种，新增的节点在旋转完成后为不为ancestor父节点，且新增节点比旋转后ancestor的父节点要小
			 * 的情况
			 * 
			 * 由于插入元素(73)比旋转后ancestor(50)的父节点(70)要大， 所以新增节点会在其右子树中
			 * 
			 * 新增63                          绕90右旋
			 *   →       50 ← ancestor       →                 50
			 *          /R\                                    /R\
			 *        20   90                                20   70
			 *       /=\   /=\                              /=\   /=\
			 *     10  40 70  95                          10  40 65 90
			 *     =   = /=\  /=\                         =   =  /= /=\ 
			 *          65 75 93 97                             63 75 95
			 *          /= =  =  =                              =  =  /=\
			 *         63                                            93 97
			 *         =                                             =  =
			 *                          
			 * 绕50左旋                       调整平衡因子
			 *   →          70                →                70
			 *              /=\                                /=\
			 *            50   90                            50   90
			 *           /R\   /=\                          /=\   /R\
			 *         20  65 75 95                       20  65 75 95
			 *        /=\  /= =  /=\                     /=\  /L    /=\
			 *       10 40 63   93 97                   10 40 63   93 97
			 *       =  =  =    =  =                    =  =  =    =  =         
			 */
            ancestor.parent.right.balanceFactor = 'R';
            ancestor.balanceFactor = '=';
            adjustPath(ancestor, inserted);
        }
    }

    /**
     * 删除指定节点
     *
     * @param elem
     * @return boolean
     */
    public boolean remove(E elem) {
        Entry<E> e = getEntry(elem);
        if (e == null)
            return false;
        deleteEntry(e);
        return true;
    }

    private Entry<E> getEntry(E e) {
        Entry<E> tmp = root;
        int c;
        while (tmp != null) {
            c = e.compareTo(tmp.elem);
            if (c == 0) {
                return tmp;
            } else if (c < 0) {
                tmp = tmp.left;
            } else {
                tmp = tmp.right;
            }
        }
        return null;
    }

    private void deleteEntry(Entry<E> p) {
        if (p.left != null && p.right != null) {

            Entry<E> s = successor(p);

            p.elem = s.elem;

            p = s;
        }

        if (p.left == null && p.right == null) {

            if (p.parent == null) {
                root = null;
            } else if (p == p.parent.left) {
                p.parent.left = null;
            } else {
                p.parent.right = null;
            }

        } else {

            Entry<E> rep;
            if (p.left != null) {
                rep = p.left;
            } else {
                rep = p.right;
            }

            rep.parent = p.parent;

            if (p.parent == null) {
                root = rep;
            } else if (p == p.parent.left) {
                p.parent.left = rep;
            } else {
                p.parent.right = rep;
            }

        }
        fixAfterDeletion(p.elem, p.parent);

        p.parent = null;
        p.left = null;
        p.right = null;

        size--;
    }

    /**
     * 查找指定节点的中序遍历序列的直接后继节点，此方法的实现与二叉搜索树类（BinSearchTree.java）的此方法是
     * 一样的，具体的思路请参考二叉搜索树类中的相应方法
     *
     * @param e 需要查找哪个节点的直接后继节点
     * @return Entry<E> 直接后继节点
     */
    private Entry<E> successor(Entry<E> e) {
        if (e == null) {
            return null;
        }//如果待找的节点有右子树，则在右子树上查找
        else if (e.right != null) {
            //默认后继节点为右子节点（如果右子节点没有左子树时即为该节点）
            Entry<E> p = e.right;
            while (p.left != null) {
                //注，如果右子节点的左子树不为空，则在左子树中查找，且后面找时要一直向左拐
                p = p.left;
            }
            return p;
        }//如果待查节点没有右子树，则要在祖宗节点中查找后继节点
        else {

            //默认后继节点为父节点（如果待查节点为父节点的左子树，则后继为父节点）
            Entry<E> p = e.parent;
            Entry<E> c = e;//当前节点，如果其父不为后继，则下次指向父节点
            //如果待查节点为父节点的右节点时，继续向上找，一直要找到c为左子节点，则p才是后继
            while (p != null && c == p.right) {
                c = p;
                p = p.parent;
            }
            return p;
        }
    }


    /**
     * 删除节点后平衡调整实现
     *
     * @param elem 被删除节点的数据域
     * @param ancestor 被删除节点的祖先节点，从父节点向上迭代
     */
    protected void fixAfterDeletion(E elem, Entry<E> ancestor) {

        boolean heightHasDecreased = true;//树的高度是否还需要减小

		/*
		 * 1、如果删除的是根节点，则ancestor为空，此时不需调整了，直接退出
		 * 2、如果删除的不是根节点，且根节点都已调整，则退出
		 * 3、如果删除的不是根节点，且树的高度不需再减小（heightHasDecreased为false），退出
		 */
        while (ancestor != null && heightHasDecreased) {

            int comp = elem.compareTo(ancestor.elem);

			/*
			 * 当要删除的节点有左右子树时，comp就会等于0，比如下面要删除33这个节点，删除方法deleteEntry
			 * 会用36替换掉33节点中的数据的elem，删除后会调用fixAfterDeletion(p.elem, p.parent)方
			 * 法来调整平衡因子，p又是指向的36，所以p.elem与p.parent.elem是相等的，都是36
			 * 
			 *            82
			 *           /L\
			 *         42   95
			 *        /=\   R\
			 *       33 48   96
			 *      /=\  /=\
			 *     29 36 43 75
			 */

            //从ancestor的右子树中删除节点
            if (comp >= 0) {
                // ancestor 的平衡因子为 '='
                if (ancestor.balanceFactor == '=') {

					/* 删除15       调整因子
					 *      20       →           20
					 *      /L\                  /L\
					 *   → 10 50                10 50
					 *     /=\                  /L
					 *    5  15                5
					 */
                    ancestor.balanceFactor = 'L';
                    heightHasDecreased = false;

                } // ancestor 的平衡因子为 'R'
                else if (ancestor.balanceFactor == 'R') {
					/* 删除15       调整因子                    下次循环调整20的因子
					 *      20       →         → 20 ← ancestor   → ...
					 *      /L\                  /L\
					 *   → 10 50                10 50
					 *     /R\ R\               /=\ R\
					 *    5  15 60              5 18 60
					 *        R\
					 *         18
					 */
                    ancestor.balanceFactor = '=';
                    ancestor = ancestor.parent;

                }// ancestor 的平衡因子为 'L'
                else if (ancestor.balanceFactor == 'L') {
                    // ancestor 的左子节点平衡因子为 '='
                    if (ancestor.left.balanceFactor == '=') {

						/* 删除60       调整因子              绕50右旋
						 *      20       →     → 20         →        20
						 *      /R\              / \                 /R\
						 *     10 50 ← ancestor 10 50 ←             10 45
						 *     /L /L\           /  /L\              /L /R\
						 *    5  45 60         5  45 60            5  35 50 ← 
						 *      /=\              /R\                     /L
						 *     35 48            35 48                   48
						 */
                        ancestor.left.balanceFactor = 'R';
                        ancestor.balanceFactor = 'L';
                        rotateRight(ancestor);
                        heightHasDecreased = false;

                    }// ancestor 的左子节点平衡因子为 'L'
                    else if (ancestor.left.balanceFactor == 'L') {

						/* 删除60       调整因子           绕50右旋   下次循环调整20的因子
						 *      20       →     → 20     →    20 ← p     → ...
						 *      /R\              / \         /R\
						 *     10 50 ← ancestor 10 50 ←     10 45
						 *     /L /L\           /  /=\      /L /=\
						 *    5  45 60         5  45 60    5  35 50 ← ancestor
						 *      /L               /=              =
						 *     35               35                      
						 */
                        Entry<E> p = ancestor.parent;
                        ancestor.balanceFactor = '=';
                        ancestor.left.balanceFactor = '=';
                        rotateRight(ancestor);
                        ancestor = p;

                    } // ancestor 的左子节点平衡因子为 'R'
                    else if (ancestor.left.balanceFactor == 'R') {

                        Entry<E> p = ancestor.parent;

                        // ancestor 的左子节点的右子节点的平衡因子为 'L'
                        if (ancestor.left.right.balanceFactor == 'L') {

							/* 删除60        调整因子               
							 *      20        →       20             
							 *      /R\               / \
							 *    10   50 ← ancestor 10  50 ← ancestor
							 *    /L\   /L\          / \  /R\
							 *   5  12 45 60        5  12 45 70
							 *  /L    /R\  R\      /     /=\  
							 * 3     42 48  70    3     42 48  
							 *          /L                 /= 
							 *         46                 46
							 *         
							 *  绕45左旋         绕50右旋           下次循环调整20的因子
							 *    →     20       →         20 ← p      → ...
							 *          /R\                /R\
							 *         10  50 ←          10   48
							 *         /L\  /R\          /L\   /=\
							 *        5 12 48 70        5  12 45 50 ← ancestor
							 *       /L   /=           /L    /=\  R\
							 *      3    45           3     42 46  70
							 *          /=\
							 *         42 46
							 */
                            ancestor.balanceFactor = 'R';
                            ancestor.left.balanceFactor = '=';

                        }// ancestor 的左子节点的右子节点的平衡因子为 'R'
                        else if (ancestor.left.right.balanceFactor == 'R') {

							/* 删除60        调整因子               
							 *      20        →       20             
							 *      /R\               / \
							 *    10   50 ← ancestor 10  50 ← 
							 *    /L\   /L\          / \  /=\
							 *   5  12 45 60        5  12 45 70
							 *  /L    /R\  R\      /     /L\  
							 * 3     42 48  70    3     42 48  
							 *           R\                 =\ 
							 *            49                 49
							 *         
							 *  绕45左旋         绕50右旋           下次循环调整20的因子
							 *    →     20       →         20 ← p      → ...
							 *          /R\                /R\
							 *         10  50 ←          10   48
							 *         /L\  /=\          /L\   /=\
							 *        5 12 48 70        5  12 45 50 ← ancestor
							 *       /L   /=\          /L    /L  /=\
							 *      3    45 49        3     42  49 70
							 *          /L
							 *         42 
							 */
                            ancestor.balanceFactor = '=';
                            ancestor.left.balanceFactor = 'L';

                        }// ancestor 的左子节点的右子节点的平衡因子为 '='
                        else {
							/* 删除60        调整因子               
							 *      20        →       20             
							 *      /R\               / \
							 *    10   50 ← ancestor 10  50 ← 
							 *    /L\   /L\          / \  /=\
							 *   5  12 45 60        5  12 45 70
							 *  /L    /R\  R\      /     /=\  
							 * 3     42 48  70    3     42 48  
							 *          /=\                /=\ 
							 *         46 49              46 49
							 *         
							 *  绕45左旋         绕50右旋           下次循环调整20的因子
							 *    →     20       →         20 ← p      → ...
							 *          /R\                /R\
							 *         10  50 ←          10   48
							 *         /L\  /=\          /L\   /=\
							 *        5 12 48 70        5  12 45  50 ← ancestor
							 *       /L   /=\          /L    /=\   /=\
							 *      3    45 49        3     42 46 49 70
							 *          /=\
							 *         42 46
							 */
                            ancestor.balanceFactor = '=';
                            ancestor.left.balanceFactor = '=';

                        }
                        ancestor.left.right.balanceFactor = '=';
                        rotateLeft(ancestor.left);
                        rotateRight(ancestor);
                        ancestor = p;
                    }
                }

            }
            //从ancestor的左子树中删除节点,与上面是对称的
            else if (comp < 0) {

                if (ancestor.balanceFactor == '=') {

                    ancestor.balanceFactor = 'R';
                    heightHasDecreased = false;
                } else if (ancestor.balanceFactor == 'L') {

                    ancestor.balanceFactor = '=';
                    ancestor = ancestor.parent;

                } else if (ancestor.balanceFactor == 'R') {

                    if (ancestor.right.balanceFactor == '=') {

                        ancestor.balanceFactor = 'R';
                        ancestor.right.balanceFactor = 'L';
                        rotateLeft(ancestor);
                        heightHasDecreased = false;

                    } else if (ancestor.right.balanceFactor == 'R') {

                        Entry<E> p = ancestor.parent;
                        ancestor.balanceFactor = '=';
                        ancestor.right.balanceFactor = '=';
                        rotateLeft(ancestor);
                        ancestor = p;

                    } else if (ancestor.right.balanceFactor == 'L') {

                        Entry<E> p = ancestor.parent;
                        if (ancestor.right.left.balanceFactor == 'R') {

                            ancestor.balanceFactor = 'L';
                            ancestor.right.balanceFactor = '=';

                        } else if (ancestor.right.left.balanceFactor == 'L') {

                            ancestor.balanceFactor = '=';
                            ancestor.right.balanceFactor = 'R';

                        } else {

                            ancestor.balanceFactor = '=';
                            ancestor.right.balanceFactor = '=';

                        }
                        ancestor.right.left.balanceFactor = '=';
                        rotateRight(ancestor.right);
                        rotateLeft(ancestor);
                        ancestor = p;

                    }
                }
            }
        }
    }

    public boolean contains(E o) {

        Entry<E> e = root;

        int comp;

        while (e != null) {

            comp = o.compareTo(e.elem);
            if (comp == 0)
                return true;
            else if (comp < 0)
                e = e.left;
            else
                e = e.right;

        }
        return false;
    }

    //验证树是否是平衡二叉树
    public boolean isAVL() {

        return checkAVL(root);

    }

    /**
     * 验证指定的树是否是平衡二叉树
     * @param p
     * @return
     */
    public static <E extends Comparable<E>> boolean checkAVL(Entry<E> p) {

        if (p == null)
            return true;
        //左子树与右子树绝对值不能超过 1，并且左右子树也是平衡二叉树
        return (Math.abs(h(p.left) - h(p.right)) <= 1 && checkAVL(p.left) && checkAVL(p.right));

    }

    /**
     * 求指定节点的高度
     * @param <E>
     * @param p
     * @return
     */
    protected static <E extends Comparable<E>> int h(Entry<E> p) {

        if (p == null)
            return -1;
        return 1 + Math.max(h(p.left), h(p.right));
    }

    /**
     * 树的高度
     * @return
     */
    public int height() {

        return h(root);

    }

    //树的高度非递归求法
    public int heightIter() {

        int height = -1;
        for (Entry<E> temp = root; temp != null; height++)
            if (temp.balanceFactor == 'L')
                temp = temp.left;
            else
                temp = temp.right;
        return height;
    }

    @Override
    public Iterator<E> iterator() {
        return new TreeIterator();
    }

    @Override
    public int size() {
        return size;
    }

    public static void main(String[] args) {
        BaseAvlTree<Integer> myTree = new BaseAvlTree<Integer>();
        Random random = new Random();
        System.out.print("随机产生的节点为：");
        int num = 0;
        //直到树的节点数为n止  
        while (myTree.size() < 10) {
            num = new Integer(random.nextInt(100));
            myTree.add(num);
            System.out.print(num + " ");
        }
        System.out.println("");
        if (myTree.isAVL()) {
            System.out.println("这棵平衡二叉树的总节点数：" + myTree.size());
            System.out.println("这棵平衡二叉树的高度是：" + myTree.height());
            System.out.println("在树中查找 " + num + " 节点:"
                    + myTree.contains(new Integer(num)));
            System.out.println("在树中查找 100 节点:" + myTree.contains(new Integer(100)));
            System.out.print("中序遍历：");
            Iterator<Integer> itr = myTree.iterator();
            while (itr.hasNext()) {
                System.out.print(itr.next() + " ");
            }
            System.out.println("");

            myTree.remove(num);
            System.out.print("删除节点 " + num + " 后遍历：");
            itr = myTree.iterator();
            while (itr.hasNext()) {
                System.out.print(itr.next() + " ");
            }
            System.out.println("");

            System.out.println("使用迭代器删除所有节点");
            itr = myTree.iterator();
            while (itr.hasNext()) {
                itr.next();
                itr.remove();
            }
            System.out.println("删除后树的总节点数：" + myTree.size());
        } else {
            System.out.println("failure");
        }
    }

}