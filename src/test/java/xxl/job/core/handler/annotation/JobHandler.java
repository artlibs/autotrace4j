package xxl.job.core.handler.annotation;

import java.lang.annotation.*;

/**
 * annotation for job handler
 * @author 2016-5-17 21:06:49
 */
// remove since v2.2.0
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JobHandler {

    String value() default "";

}
