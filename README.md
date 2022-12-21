# Android 操作data 本次更新主要解决兼容android13问题,在android13后无法直接申请data目录,需要申请data目录下单个应用操作权限,具体可以下载例子查看

这是一个android 操作data目录 的项目 目前兼容了 dataTools(android11 android12)  dataToolsApi33(android13)
请注意build.gradle 下如下依赖请务必复制到你的项目中,本项目目前免费开源且授权给任何个体和组织进行商用。没有任何限制如果对你有帮助请给我一个stars

  //此处依赖非常重要可以直接复制到你的工程,用于解决部分手机删除文件失败,和文件操作性能低问题
    implementation 'androidx.documentfile:documentfile:1.1.0-alpha01'
    implementation files('libs\\switcher-1.1.1.jar')
  //此处依赖非常重要可以直接复制到你的工程,用于解决部分手机删除文件失败,和文件操作性能低问题




# 如遇错误欢迎加群反馈
# 忧愁的qq:2557594045
# 欢迎加入安卓开发交流群 970905285
# 欢迎加入安卓开发交流2群 1034191770
