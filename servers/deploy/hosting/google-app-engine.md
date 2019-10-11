---
title: GAE
caption: Google App Engine
category: servers
permalink: /servers/deploy/hosting/google-app-engine.html
ktor_version_review: 1.0.0
---

GoogleAppEngineのフルのサンプルはここで見れます:
<https://github.com/ktorio/ktor-samples/tree/master/deployment/google-appengine-standard>
{: .note.example}

## 準備

初めに`gcloud`CLIをインストールする必要があります。
ここから使うことができます:
<https://cloud.google.com/sdk/docs/>
そしてインストールのためには記載されているステップに従ってください。

例えば、macOSにおけるセットアップは以下のようになります:

```
> wget https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-194.0.0-darwin-x86_64.tar.gz
> tar -xzf google-cloud-sdk-194.0.0-darwin-x86_64.tar.gz
> cd google-cloud-sdk

> ./install.sh
Welcome to the Google Cloud SDK!

To help improve the quality of this product, we collect anonymized usage data
and anonymized stacktraces when crashes are encountered; additional information
is available at <https://cloud.google.com/sdk/usage-statistics>. You may choose
to opt out of this collection now (by choosing 'N' at the below prompt), or at
any time in the future by running the following command:

    gcloud config set disable_usage_reporting true

Do you want to help improve the Google Cloud SDK (Y/n)?  n


Your current Cloud SDK version is: 194.0.0
The latest available version is: 194.0.0

┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                  Components                                                 │
├───────────────┬──────────────────────────────────────────────────────┬──────────────────────────┬───────────┤
│     Status    │                         Name                         │            ID            │    Size   │
├───────────────┼──────────────────────────────────────────────────────┼──────────────────────────┼───────────┤
│ Not Installed │ App Engine Go Extensions                             │ app-engine-go            │ 151.3 MiB │
│ Not Installed │ Cloud Bigtable Command Line Tool                     │ cbt                      │   4.0 MiB │
│ Not Installed │ Cloud Bigtable Emulator                              │ bigtable                 │   3.8 MiB │
│ Not Installed │ Cloud Datalab Command Line Tool                      │ datalab                  │   < 1 MiB │
│ Not Installed │ Cloud Datastore Emulator                             │ cloud-datastore-emulator │  17.9 MiB │
│ Not Installed │ Cloud Datastore Emulator (Legacy)                    │ gcd-emulator             │  38.1 MiB │
│ Not Installed │ Cloud Pub/Sub Emulator                               │ pubsub-emulator          │  33.4 MiB │
│ Not Installed │ Emulator Reverse Proxy                               │ emulator-reverse-proxy   │  14.5 MiB │
│ Not Installed │ Google Container Local Builder                       │ container-builder-local  │   3.7 MiB │
│ Not Installed │ Google Container Registry's Docker credential helper │ docker-credential-gcr    │   2.5 MiB │
│ Not Installed │ gcloud Alpha Commands                                │ alpha                    │   < 1 MiB │
│ Not Installed │ gcloud Beta Commands                                 │ beta                     │   < 1 MiB │
│ Not Installed │ gcloud app Java Extensions                           │ app-engine-java          │ 118.9 MiB │
│ Not Installed │ gcloud app PHP Extensions                            │ app-engine-php           │  21.9 MiB │
│ Not Installed │ gcloud app Python Extensions                         │ app-engine-python        │   6.2 MiB │
│ Not Installed │ gcloud app Python Extensions (Extra Libraries)       │ app-engine-python-extras │  27.8 MiB │
│ Not Installed │ kubectl                                              │ kubectl                  │  12.2 MiB │
│ Installed     │ BigQuery Command Line Tool                           │ bq                       │   < 1 MiB │
│ Installed     │ Cloud SDK Core Libraries                             │ core                     │   7.4 MiB │
│ Installed     │ Cloud Storage Command Line Tool                      │ gsutil                   │   3.4 MiB │
└───────────────┴──────────────────────────────────────────────────────┴──────────────────────────┴───────────┘
To install or remove components at your current SDK version [194.0.0], run:
  $ gcloud components install COMPONENT_ID
  $ gcloud components remove COMPONENT_ID

To update your SDK installation to the latest version [194.0.0], run:
  $ gcloud components update


Modify profile to update your $PATH and enable shell command
completion?

Do you want to continue (Y/n)?  Y

The Google Cloud SDK installer will now prompt you to update an rc
file to bring the Google Cloud CLIs into your environment.

Enter a path to an rc file to update, or leave blank to use
[/Users/user/.zshrc]:
Backing up [/Users/user/.zshrc] to [/Users/user/.zshrc.backup].
[/Users/user/.zshrc] has been updated.

==> Start a new shell for the changes to take effect.


For more information on how to get started, please visit:
  https://cloud.google.com/sdk/docs/quickstarts
```
{: .compact}

その後、新しいshellが起動でき、`gcloud`CLIにアクセスできます。例えば:

```
> gcloud --version
Google Cloud SDK 194.0.0
bq 2.0.30
core 2018.03.16
gsutil 4.29
```

CLIでいくつかのコンポーネントもインストールする必要があります。（`gcloud components install app-engine-java`）:

```
> gcloud components install app-engine-java


Your current Cloud SDK version is: 194.0.0
Installing components from version: 194.0.0

┌────────────────────────────────────────────────────┐
│        These components will be installed.         │
├──────────────────────────────┬─────────┬───────────┤
│             Name             │ Version │    Size   │
├──────────────────────────────┼─────────┼───────────┤
│ gRPC python library          │         │           │
│ gRPC python library          │   1.9.1 │   7.6 MiB │
│ gcloud app Java Extensions   │  1.9.63 │ 118.9 MiB │
│ gcloud app Python Extensions │  1.9.67 │   6.2 MiB │
└──────────────────────────────┴─────────┴───────────┘

For the latest full release notes, please visit:
  https://cloud.google.com/sdk/release_notes

Do you want to continue (Y/n)?  Y

╔════════════════════════════════════════════════════════════╗
╠═ Creating update staging area                             ═╣
╠════════════════════════════════════════════════════════════╣
╠═ Installing: gRPC python library                          ═╣
╠════════════════════════════════════════════════════════════╣
╠═ Installing: gRPC python library                          ═╣
╠════════════════════════════════════════════════════════════╣
╠═ Installing: gcloud app Java Extensions                   ═╣
╠════════════════════════════════════════════════════════════╣
╠═ Installing: gcloud app Python Extensions                 ═╣
╠════════════════════════════════════════════════════════════╣
╠═ Creating a backup and activating a new installation      ═╣
╚════════════════════════════════════════════════════════════╝

Performing post-processing steps...done.

Update done!
```
{: .compact }

gradleとオフィシャルプラグインである`appengine-gradle-plugin`を使うこともできます。
その場合`build.gradle`は以下のようになります:

{% capture build-gradle %}
```groovy
buildscript {
    ext.appengine_version = '1.9.60'
    ext.appengine_plugin_version = '1.3.4'

    repositories {
        jcenter()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath "com.google.cloud.tools:appengine-gradle-plugin:$appengine_plugin_version"
    }
}

apply plugin: 'kotlin'
apply plugin: 'war'
apply plugin: 'com.google.cloud.tools.appengine'

// appengine does not honor this property, so we are forced to use deep Maven tree layout
// webAppDirName = file('webapp')

sourceSets {
    main.kotlin.srcDirs = [ 'src/main/kotlin' ]
}

repositories {
    jcenter()
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    compile "io.ktor:ktor-server-servlet:$ktor_version"
    compile "io.ktor:ktor-html-builder:$ktor_version"
    compile "org.slf4j:slf4j-jdk14:$slf4j_version"

    providedCompile "com.google.appengine:appengine:$appengine_version"
}

kotlin.experimental.coroutines = 'enable'

task run(dependsOn: appengineRun)
```
{% endcapture %}

{% include tabbed-code.html
    tab1-title="build.gradle" tab1-content=build-gradle
    no-height="true"
%}

一旦すべて設定されたら、`appengineRun` gradle taskを使うことで、アプリケーションをローカルで起動することができます:

この場合、ktorのサンプルリポジトリ<https://github.com/ktorio/ktor-samples/>のルートにおいてこれらのコマンドが実行できます:  

```
./gradlew :google-appengine-standard:appengineRun
```

サーバーを<http://localhost:8080/>で起動し、adminを<http://localhost:8080/_ah/admin>で起動することができます。

## デプロイ

初めに、プロジェクトを作成する必要があります。
`gcloud projects create demo-demo-123456 --set-as-default`

```
> gcloud projects create demo-demo-123456 --set-as-default
Create in progress for [https://cloudresourcemanager.googleapis.com/v1/projects/demo-demo-123456].
Waiting for [operations/pc.7618150612308930095] to finish...done.
Updated property [core/project] to [demo-demo-123456].
```

そして、`gcloud app create`を使い、アプリケーションを作成する必要があります:

```
> gcloud app create
You are creating an app for the project [demo-demo-123456].
WARNING: Creating an App Engine application for a project is irreversible, and the region
cannot be changed. More information about regions is at
<https://cloud.google.com/appengine/docs/locations>.

Please choose the region where you want your App Engine application
located:

 [1] europe-west2  (supports standard and flexible)
 [2] us-central    (supports standard and flexible)
 [3] europe-west   (supports standard and flexible)
 [4] europe-west3  (supports standard and flexible)
 [5] us-east1      (supports standard and flexible)
 [6] us-east4      (supports standard and flexible)
 [7] asia-northeast1 (supports standard and flexible)
 [8] asia-south1   (supports standard and flexible)
 [9] australia-southeast1 (supports standard and flexible)
 [10] southamerica-east1 (supports standard and flexible)
 [11] northamerica-northeast1 (supports standard and flexible)
 [12] cancel
Please enter your numeric choice:  1

Creating App Engine application in project [demo-demo-123456] and region [europe-west2]....done.
Success! The app is now created. Please use `gcloud app deploy` to deploy your first app.
```

これで、`gradle appengineDeploy`を使ってアプリケーションをデプロイすることができます:

```
> gradle :google-appengine-standard:appengineDeploy
Starting a Gradle Daemon (subsequent builds will be faster)
Reading application configuration data...
Mar 23, 2018 6:32:09 AM com.google.apphosting.utils.config.IndexesXmlReader readConfigXml
INFORMATION: Successfully processed /Users/user/projects/ktor-samples/deployment/google-appengine-standard/build/exploded-google-appengine-standard/WEB-INF/appengine-generated/datastore-indexes-auto.xml


Beginning interaction for module default...
0% Scanning for jsp files.
0% Generated git repository information file.
Success.
Temporary staging for module default directory left in /Users/user/projects/ktor-samples/deployment/google-appengine-standard/build/staged-app
Services to deploy:

descriptor:      [/Users/user/projects/ktor-samples/deployment/google-appengine-standard/build/staged-app/app.yaml]
source:          [/Users/user/projects/ktor-samples/deployment/google-appengine-standard/build/staged-app]
target project:  [demo-demo-123456]
target service:  [default]
target version:  [20180323t063212]
target url:      [https://demo-demo-123456.appspot.com]


Beginning deployment of service [default]...
Some files were skipped. Pass `--verbosity=info` to see which ones.
You may also view the gcloud log file, found at
[/Users/user/.config/gcloud/logs/2018.03.23/06.32.10.739209.log].
#============================================================#
#= Uploading 38 files to Google Cloud Storage               =#
#============================================================#
File upload done.
Updating service [default]...
..............done.
Setting traffic split for service [default]...
.......done.
Deployed service [default] to [https://demo-demo-123456.appspot.com]

You can stream logs from the command line by running:
  $ gcloud app logs tail -s default

To view your application in the web browser run:
  $ gcloud app browse

BUILD SUCCESSFUL in 42s
6 actionable tasks: 2 executed, 4 up-to-date
```
{: .compact }

これで`gcloud app browse`コマンドで、ブラウザであなたのアプリケーションを見ることができます。
このコマンドはアプリケーションを開きます。
この場合、https://demo-demo-123456.appspot.comです。
