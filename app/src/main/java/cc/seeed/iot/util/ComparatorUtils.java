package cc.seeed.iot.util;

import java.util.Comparator;

import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;

/**
 * Created by lenovo on 2016/4/22.
 */
public class ComparatorUtils {
    /**
     * 去掉前缀后,按照自然排序方式排序
     */
    public static class ComparatorName implements Comparator {
        public int compare(Object arg0, Object arg1) {
            GroverDriver bean1 = (GroverDriver) arg0;
            GroverDriver bean2 = (GroverDriver) arg1;

            int num1 = (int) ToolUtil.getSimpleName(bean1.GroveName).charAt(0);
            int num2 = (int) ToolUtil.getSimpleName(bean2.GroveName).charAt(0);
            if (num1 < num2) {
                return -1;
            } else if (num1 == num2) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    /**
     * 按照在线状态来排序
     */
    public static class ComparatorNode implements Comparator {
        public int compare(Object arg0, Object arg1) {
            Node node1 = (Node) arg0;
            Node node2 = (Node) arg1;

            if (node1.online && !node2.online) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    /**
     * 按字符串长度离排序
     */
    public static class ComparatorStringLength implements Comparator {
        public int compare(Object arg0, Object arg1) {
            String bean1 = (String) arg0;
            String bean2 = (String) arg1;

            if (bean1.length() < bean2.length()) {
                return -1;
            } else if (bean1.length() == bean2.length()) {
                return 0;
            } else {
                return 1;
            }
        }
    }

}
