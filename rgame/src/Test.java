import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import game.SRoleBaseConfig;
import log.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.TreeMap;

/**
 * @author sunfengmao
 * @Date 2018/5/27
 */
public class Test {

    public static void main(String[] args) throws Exception{
//        int i = 10;
//        Logger logger = Logger.getLogger(Test.class);
//        logger.debug("ddsf", i);
        Arrays.asList(1, 2, 3).stream()
                .forEach(p -> {
                    if(p == 2){
                        return;
                    }
                    System.out.println(p);
                });

    }

}
