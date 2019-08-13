import com.brian.config.MainConfig;
import com.sun.prism.shader.Solid_TextureYV12_AlphaTest_Loader;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MianTest {

    @Test
    public  void test01 () {
      AnnotationConfigApplicationContext acac =
              new AnnotationConfigApplicationContext(MainConfig.class);
      String[] list =  acac.getBeanDefinitionNames();
        for (int i = 0; i < list.length; i++) {
            System.out.println(list[i]);

        }
    }
    @Test
    public void test2(){
         String s="1";
        String s1=new String("1");
        String s6=new String("1");
        System.out.println(s1 ==s6);
        String s2=s+s1;
        String s3="1"+"1";
        String s5="11";//常量池
        System.out.println(s5 == s3);
        String s4=new String("11");
        System.out.println(s == s1);
        System.out.println(s2 == s3);
        System.out.println(s4 == s2);
        System.out.println(s4 == s5);

    }


}
