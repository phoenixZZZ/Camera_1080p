package com.jiuan.it.ipc.http;

public class BaseRequestConfig {

    public static final String SC = "1eb3253f3acb44c0a18b0bfbb0dac3d7";

    // 1.申请智能控注册验证码
    public static final String SV_VERIFY_PHONE = "7fd868b50ae14e10a782e638c968b1c0";

    // 2.验证手机验证码
    public static final String SV_VERIFY_PHONE_CODE = "60eeaeb3e193468b9f92f01572c927ec";

    // 3.手机账号注册
    public static final String SV_REG_PHONE = "3c84d6f8604d425f971bdce64738b334";

    // 4.手机账号登录
    public static final String SV_PHONE_LOGIN = "a3f12d3b0d46406eb6979e2de2d8dddf";

    // 5.申请重置密码验证码
    public static final String SV_VERIFY_RESET = "02e6709d84ad4ccbbbf803bf07cdd8bf";

    // 6.验证重置密码验证码
    public static final String SV_VERIFY_RESET_CODE = "3eb5acad1f7e461dbe336471456ab088";

    // 7.重置密码
    public static final String SV_RESET_CODE = "19571df63c634cd9b51adb83ec740d34";

    // 8.访问令牌刷新
    public static final String SV_REFRESH_TOKEN = "194cc5a658a34dc7915c58e384d1cca5";

    // 9.申请手机动态登录码
    public static final String SV_GET_LOGIN_CODE = "7065e4ec6ca94c23ac420b44348ecf69";

    // 10.手机动态码登录
    public static final String SV_CODE_LOGIN = "7acc561e6ed14242a808853ebd011ab6";

    // 11.获取用户个人信息资料
    public static final String SV_GET_USER_INFO = "fad9f402d346455d883772655c27453c";

    // 12.修改用户个人信息资料
    public static final String SV_SET_USER_INFO = "9d1de4bb82154534942b724bbfc417ef";

    // 13.修改密码
    public static final String SV_CHANGE_PASSWORD = "28ba708deb42460b86a9317a07d7bc03";

    // 14.个人资料中用户上传头像
    public static final String SV_UPLOAD_AVATARS = "d5be504bc4ca4f4d961b3b68ff54fcdb";

    // 15.添加设备
    public static final String SV_ADD_EQ = "bb3624f9d9624922bed554eef28d09a2";

    // 16.删除设备
    public static final String SV_DEL_EQ = "fc8f768ce0054c1c8bbd11c33ff709aa";

    // 17.修改设备
    public static final String SV_UPDATE_EQ = "d1c789191196479c89796edcd1bd22da";

    // 18.设备资料中上传设备头像
    public static final String SV_UPLOAD_EQ_AVATAR = "f32854dc09be4151bfddc4464966dadb";

    // 19.用户反馈信息上传
    public static final String SV_USER_FEEDBACK = "085fa75cb15c48fdb00fdde762b7bde5";

    // 20.用户反馈图片上传
    public static final String SV_USER_FEEDBACK_IMG = "f9afeaee580f4a50ba32ca6df494a102";

    // 21.分享我的设备
    public static final String SV_SHARE_EQ = "3be2f85c97fe4e808e0bb2b0bcfd24f5";

    // 22.查看邀请我的信息
    public static final String SV_MY_INVITATION = "2e766c09e49f489ebb17e8f3091c412b";

    // 23.同意/拒绝邀请信息
    public static final String SV_INVITATION_RESPONSE = "6520206dffed482d8e113a094b677084";

    // 24.获取用户所有设备信息
    public static final String SV_MY_EQUIPS = "b3975723e47840bcbeb5358b5b617019";

    // 25.获取指定设备的分享信息
    public static final String SV_MY_SHARINGS = "e35147b1cfb54769aff1a53cb874f6fb";

    // 26.删除被分享者与设备的关联
    public static final String SV_DEL_SHARINGS = "79ad428c37994b8b8ac86aef3cbcaba9";

    // 27.获取指定设备最新N条指令信息
    public static final String SV_GET_TOP_COMMANDS = "7313cd5b04754dd7b0b0af8daf975d10";

    // 28.获取指定设备给定时间之前的N条指令数据
    public static final String SV_GET_OLD_COMMANDS = "6d877b35a465400f817603204b70cb8c";

    // 29.客户端向设备发送指令消息
    public static final String SV_SEND_COMMAND = "3826ba1ab9274a538694479c9f3a12a0";

    // 30.同步客户端指令数据
    public static final String SV_SYN_CLIENT_DATA = "bba779670fbe47d0871ad8fa1cfa54a6";

    // 31.获取设备列表（临时）
    public static final String SV_GET_EQUIP_LIST = "35d07db112374c18bbbc7b1c50ca6ec2";

    // 32.我的好友列表
    public static final String SV_GET_FRIENDS_LIST = "404275e5d3844d72ae0d1c8ba4fea084";

    // 33.搜索好友信息
    public static final String SV_SEARCH_FRIEND_INFO = "a6893650437b460cb22977b45c00095c";

    // 34.添加好友
    public static final String SV_ADD_FRIEND_INFO = "bcab0148e9e24f5ebecb9a5f8b00276f";

    // 35.我的设备圈列表
    public static final String SV_MY_CRICLES = "b3bdc377e6384769bbbd4c15ad1722e0";

    // 36.添加设备圈
    public static final String SV_ADD_CRICLE = "d6e081af26d541c4b8605f41deab990c";

    // 37.修改设备圈
    public static final String SV_UPDATE_CRICLE = "9881e13c98004a32aa17f2873ce72500";

    // 38.获取设备圈详细信息
    public static final String SV_GET_CRICLE_DETAIL = "6ff60489a647470dbc501985e3bd305c";

    // 39.设备圈中发送信息
    public static final String SV_SEND_CIRCLE_MESSAGES = "3adb1e8bbc0542f5a799b4884b971bae";

    // 40．获取指定设备圈给定时间之前的N条指令数据（老数据）
    public static final String SV_GET_OLD_CIRCLEMESSAGES = "bec7a37df47c4f6d8379bf7cd44bf12f";

    // 41．获取指定设备圈给定时间之后的N条指令数据（新数据）
    public static final String SV_GET_TOP_CIRCLEMESSAGES = "3ab4c5e0225e4cbfacf1fb4664bd25dd";

    // 42．删除好友
    public static final String SV_DEL_FRIEND = "1c954b1e0fa946ab84b0ab09b5c59dde";

    // 43．删除设备圈
    public static final String SV_DEL_CIRCLE = "cad91dd84ab64d63ae924935640068d7";

    // 44．提供指令及权限列表
    public static final String SV_GET_TOTALSET = "ef8dfd8ea9384a54b53883f2155d6bf6";

    // 45．修改设备默认指令列表
    public static final String SV_UPDATE_DEFAULTSET = "3bb570ded94d486084ae5e786a0e8231";

    // 46．修改用户权限
    public static final String SV_UPDATE_USERAUTH = "4c930f11b1534d8f86225b9affeed24d";

    // 47．获取白盒ID列表
    public static final String SV_GET_WHITEBOXIDS = "01b5cda5fb69471ba05ba58322ae3c8b";

    // 48．获取好友验证消息
    public static final String SV_GET_FRIENDMESSAGE = "f9f212f37c574f5885a64a17f6b9240a";

    // 49．接收/拒绝邀请
    public static final String SV_RESPONSE_FRIENDMESSAGE = "76109e9d5155431ba1716586bdad7cf5";

    // 50．上传白盒子绑定信息
    public static final String SV_BIND_BOXANDEQUIP = "407b42c5598645b4baed7e05fa37057f";

    // 51．用户绑定白盒子
    public static final String SV_BIND_WBOXUSER = "42e279ac8ff2400b9d455f3becd1580e";

    // 52．获取我的设备列表（设备和白盒）
    public static final String SV_MY_BOXEQUIP = "2e31a9f225d64ef1a5af556910564a38";

    // 53．获取白盒详细信息
    public static final String SV_GET_WBOXDETAIL = "535d3126e718408cb964b26fe53bd6a3";

    // 54．给设备发送指令
    public static final String SV_SEND_EQUIPMESSAGE = "36102b640c6146eeb5a0dc4bdd4af43f";

    // 55．修改白盒信息
    public static final String SV_UPDATE_WHITEBOX = "4ac965eeb3974fcfbbd48dcb62c62474";

    // 56．根据设备唯一ID获取设备信息
    public static final String SV_GET_EQUIPINFO = "1b21409c595c43e48fc9ac9681a24eac";

    // 57．获取设备指令
    public static final String SV_GET_EQUIPSET = "dfd086d6908443f9453a8a2996191e6";

    // 58．删除设备（白盒）占有者与设备（白盒）的关联
    public static final String SV_DEL_WBOXEQ = "f11e81ada19a4ff596cd8772a5ae88f1";

    // 59．手机端向云端获取发送指令的令牌
    public static final String SV_GET_ORDERTOKEN = "5e9132cc9fb045dfba7f9b1b8eab58eb";

    // 获取图片的上传状态
    public static final String SV_UPLOAD_STATE= "733882b4689248c6985be924e2959108";

    // 获取设备文件目录
    public static final String SV_TYPE_CODE = "e05a5b83e2884714b2d3f955bea7b187";

    // 获取目录详细信息
    public static final String SV_TYPE_CODE_DETAIL= "b292891bce5e49d69dbcb15e30044e2d";

    //获取全景图片
    public static final String SV_MERGED_PIC= "56198f6d1c494ae9b5d1726620256f72";

    // 检查升级
    public static final String SV_LAST_VER = "64c8fbb5ab0844689df66917d60a62b5";

    // 获取指定版本的固件信息
    public static final String SV_VER_INFO= "b5e5794e2a4e4e8b8c1d3de6a5e0851d";




}
