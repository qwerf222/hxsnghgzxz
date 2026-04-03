# 公招选择器

一个简约风格的 Android 标签组合筛选器，为幻想少女工会游戏制作。 <br>
数据来源于《幻想少女工会》官方讨论群1的群文件公招角色25-12-17.xlsx

### 使用界面
![微信图片_20260403173747_3_227](https://github.com/user-attachments/assets/d19d254b-191d-4921-8296-7861a675d93a)


## 已实现

- 点击 `1~5` 个标签进行筛选
- 结果按 **标签组合 → 角色列表** 展示，而不是按角色命中标签数展示
- 自动列出所有 `1~3` 标签组合的有效结果
- 排序优先级：
  1. 三标签组合优先
  2. 保底星级高优先
  3. 最高星级高优先
  4. 标签价值高优先
  5. 角色数量更少的精确组合优先
- 主页会根据数据源动态渲染标签分组，目前支持 `职业 / 种族 / 属性 / 地形`
- 角色与标签数据来自工作区中的 `副本公招角色25-12-17.xlsx`

## 数据生成链路

- Excel 源文件：`副本公招角色25-12-17.xlsx`
- 转换脚本：`tools/generate_recruit_data.py`
- 生成结果：`app/src/main/java/com/gzxz/recruit/GeneratedRecruitData.kt`
- 运行时入口：`app/src/main/java/com/gzxz/recruit/RecruitData.kt`

当 Excel 更新后，可重新生成 Kotlin 数据：

```zsh
cd "./公招选择器"
python3 -m pip install --user -r tools/requirements.txt
python3 tools/generate_recruit_data.py
```

## 主要文件

- `app/src/main/java/com/gzxz/MainActivity.kt`：主页交互、动态标签分组与结果卡片渲染
- `app/src/main/java/com/gzxz/recruit/RecruitModels.kt`：标签分组 / 标签 / 角色模型
- `app/src/main/java/com/gzxz/recruit/RecruitCalculator.kt`：组合计算与排序
- `app/src/main/java/com/gzxz/recruit/GeneratedRecruitData.kt`：由 Excel 生成的静态 Kotlin 数据
- `app/src/main/res/layout/activity_main.xml`：主页布局

## 本地验证

```zsh
cd "./公招选择器"
export JAVA_HOME="$('/usr/libexec/java_home' -v 17)"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew test
./gradlew assembleDebug
```


