pluginManagement {
    repositories {
//        maven("https://maven.aliyun.com/repository/google") //替换google()
//        maven("https://maven.aliyun.com/repository/central")
//        maven("https://maven.aliyun.com/repository/public")
//        maven("https://maven.aliyun.com/repository/gradle-plugin")

        maven("https://jitpack.io")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        maven("https://maven.aliyun.com/repository/google") //替换google()
//        maven("https://maven.aliyun.com/repository/central")
//        maven("https://maven.aliyun.com/repository/public")
//        maven("https://maven.aliyun.com/repository/gradle-plugin")

        maven("https://jitpack.io")
        google()
        mavenCentral()
    }
}

rootProject.name = "MangaReader"
include(":app")
include(":pagecurl")
include(":swipeable_cards")
include(":PdfiumAndroid")
include(":crashhandler")
