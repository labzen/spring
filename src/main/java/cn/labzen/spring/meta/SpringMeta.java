package cn.labzen.spring.meta;

import cn.labzen.meta.component.DeclaredComponent;

import javax.annotation.Nonnull;

public class SpringMeta implements DeclaredComponent {

  @Nonnull
  @Override
  public String description() {
    return "对基于Spring的企业项目做增强";
  }

  @Nonnull
  @Override
  public String mark() {
    return "Labzen-Spring";
  }

  @Nonnull
  @Override
  public String packageBased() {
    return "cn.labzen.spring";
  }
}
