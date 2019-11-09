# android-utils

- [adapter](#adapter)
- [anim](#anim)
- [app](#app)
- [general](#general)
  - [storage](#storage)
  - [file](#file)
  - [io](#io)
  - [concurrent](#concurrent)
  - [simplify](#simplify)
  - [reflect](#reflect)
- [net](#net)

工具类的说明文档和类在同一层目录，命名为 desc_xxx_xxx。😭

接口抽象能力很重要，希望能够提高此能力。

## adapter

适配器相关

- ListView Adapter

[BaseLvAdapter](./utilslib/src/main/java/io/l0neman/utils/adapter/desc_base_lvadapter.md)

- RecyclerView Adapter

[BaseRvAdapter](./utilslib/src/main/java/io/l0neman/utils/adapter/desc_base_rvadapter.md)

- RecyclerView Adapter - 首尾布局。

[HeaderRvAdapter](./utilslib/src/main/java/io/l0neman/utils/adapter/desc_header_rvadapter.md)

## anim

动画相关

- 动画创建器 - 链式。

[AnimatorCreator](./utilslib/src/main/java/io/l0neman/utils/anim/desc_animator_creator.md)

## app

应用相关

- 权限申请工具

[Permission](./utilslib/src/main/java/io/l0neman/utils/app/desc_permission_utils.md)

## general

通用工具

### storage

存储相关

- 简单存储工具

[DirStore](./utilslib/src/main/java/io/l0neman/utils/general/storage/desc_dir_store.md)

### file

文件相关

- 文件工具

[EasyFile](./utilslib/src/main/java/io/l0neman/utils/general/file/desc_easy_file.md)

### io

输入输出相关

- 关闭工具

[Closer](./utilslib/src/main/java/io/l0neman/utils/general/io/desc_closer.md)

### concurrent

并发相关

- 线程池构建器 - 链式。

[ExecutorCreator](./utilslib/src/main/java/io/l0neman/utils/general/concurrent/desc_executor_creator.md)

### simplify

简化相关操作

- 命令执行工具

[CommandExecutor](./utilslib/src/main/java/io/l0neman/utils/general/simplify/desc_command_executor.md)

### reflect

反射相关

- 反射工具 - 链式。

[Reflect](./utilslib/src/main/java/io/l0neman/utils/general/reflect/desc_reflect_utils.md)

## net

网络请求相关

- 网络请求工具

[HttpUtils](./utilslib/src/main/java/io/l0neman/utils/net/desc_http_utils.md)

