Springboot中使用redis进行api防刷限流

分类: SpringBoot开发undefined

api限流的场景#
限流的需求出现在许多常见的场景中

秒杀活动，有人使用软件恶意刷单抢货，需要限流防止机器参与活动
某api被各式各样系统广泛调用，严重消耗网络、内存等资源，需要合理限流
淘宝获取ip所在城市接口、微信公众号识别微信用户等开发接口，免费提供给用户时需要限流，更具有实时性和准确性的接口需要付费。
api限流实战#
首先我们编写注解类AccessLimit，使用注解方式在方法上限流更优雅更方便！三个参数分别代表有效时间、最大访问次数、是否需要登录，可以理解为 seconds 内最多访问 maxCount 次。

Copy
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimit {
    int seconds();
    int maxCount();
    boolean needLogin() default true;
}
限流的思路

通过路径:ip的作为key，访问次数为value的方式对某一用户的某一请求进行唯一标识
每次访问的时候判断key是否存在，是否count超过了限制的访问次数
若访问超出限制，则应response返回msg:请求过于频繁给前端予以展示
Copy
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AccessLimtInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (null == accessLimit) {
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            if (needLogin) {
                //判断是否登录
            }

            String key = request.getContextPath() + ":" + request.getServletPath() + ":" + ip ;

            Integer count = redisService.get(key);

            if (null == count || -1 == count) {
                redisService.set(key, 1);
                redisService.expire(seconds);
                return true;
            }

            if (count < maxCount) {
                redisService.inCr(key);
                return true;
            }

            if (count >= maxCount) {
//                response 返回 json 请求过于频繁请稍后再试
                return false;
            }
        }

        return true;
    }
}
注册拦截器并配置拦截路径和不拦截路径

Copy
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// extends WebMvcConfigurerAdapter 已经废弃，java 8开始直接继承就可以
@Configuration
public class IntercepterConfig  implements WebMvcConfigurer {
    @Autowired
    private AccessLimtInterceptor accessLimtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessLimtInterceptor)
                .addPathPatterns("/拦截路径")
                .excludePathPatterns("/不被拦截路径 通常为登录注册或者首页");
    }
}
在Controller层的方法上直接可以使用注解@AccessLimit

Copy
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestControler {

    @GetMapping("accessLimit")
    @AccessLimit(seconds = 3, maxCount = 10)
    public String testAccessLimit() {
        //xxxx
        return "";
    }
}
作者： 海向

出处：https://www.cnblogs.com/haixiang/p/12012728.html

本站使用「CC BY 4.0」创作共享协议，转载请在文章明显位置注明作者及出处。