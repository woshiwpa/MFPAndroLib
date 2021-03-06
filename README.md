# MFPAndroLib
This is MFP (Mathematic language For Parallel computing) Android library project. MFP is a novel scripting programming language designed and developed by me. It can run in both JVM and Android devices. I have an Android app called Scientific Calculator Plus listed in Google's App store at https://play.google.com/store/apps/details?id=com.cyzapps.AnMath . This app includes full support to MFP language as well as many other interesting features.

Different from Scientific Calculator Plus, the MFPAndroLib repo just includes MFP source codes and a demonstration of use of MFP library. It comprises two modules. One is MFPAnLib. This module is MFP library. Its output is MFPAnLib-release.aar, i.e. MFP Android library. The other is app module. This is an example to show developers how to embed MFP Android library into their Android App project and take advantage of this programming language.

MFP Android lib binary can also be downloaded at https://github.com/woshiwpa/MFPAndroLib/tree/main/app/libs . Don't forget to download google-webrtc lib as it is a dependency of MFP lib.

Utilizing the MFP language is straight-forward. First, developers need to copy the MFPAnLib-release.aar and Google's WebRTC aar into their App project and in the build.gradle add the two lines:

    implementation files('path/to/google-webrtc-x.x.xxxx.aar')
    
    implementation files('path/to/MFPAnLib-release.aar')
    
. Then simply follow the sample codes in file AppAnMFP.java or ActivityAnMFPMain.java to run predefined MFP code saved in the assets/userdef_lib.zip file in developers' app.
MFP is object oriented and provides plentiful functions for 2D game development, complex number, matrix, (higher level) integration, 2D, polar and 3D chart, string, file operation, JSON data exchange and TCP/WebRTC communication. Clearly, MFP could save developers a significant amount of time and resources to achieve their aim. Moreover, if developers are very familiar with MFP source codes, they can call MFP lib's JAVA functions directly although this approach is not recommended.

Detailed instruction to use MFP Android lib in your app is available at https://woshiwpa.github.io/MFPLang/en/HowtoInfo/use_mfp_android_lib.html .

MFP can also run in PC with JAVA installed. The corresponding git repo is MFPLang4JVM at https://github.com/woshiwpa/MFPLang4JVM .

MFP's on-line language manual is located at https://woshiwpa.github.io/MFPLang/en/MFPIndex.html .
