create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    username     varchar(256)                            null comment '用户昵称',
    userAccount  varchar(256)                            null comment '账号',
    avatarUrl    varchar(3600)                           null comment '用户头像',
    gender       tinyint                                 null comment '性别',
    userPassword varchar(512)                            not null comment '密码',
    phone        varchar(128)                            null comment '电话',
    email        varchar(512)                            null comment '邮箱',
    userStatus   int           default 0                 not null comment '状态 0 - 正常',
    createTime   datetime      default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime      default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint       default 0                 not null comment '是否删除',
    userRole     int           default 0                 not null comment '用户角色 0 - 普通用户 1 - 管理员',
    planetCode   varchar(512)                            null comment '星球编号',
    tags         varchar(1024)                           null comment '标签 JSON 列表',
    profile      varchar(1024) default '纯情男大'        null comment '个人简介'
)
    comment '用户';


-- auto-generated definition
create table tag
(
    id         bigint auto_increment comment 'id'
        primary key,
    tagName    varchar(256)                       null comment '标签名称',
    userId     bigint                             null comment '上传标签的用户id',
    parentId   bigint                             null comment '父标签 Id',
    isParent   tinyint                            null comment '是否是父标签  0 - 不是； 1 - 是父标签',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除',
    constraint unildx_tagName
        unique (tagName) comment 'tagName的索引，让这个字段查询数据更快'
)
    comment '标签表';

create index idx_userId
    on tag (userId);

-- 队伍表
/**
   这个表可以看到： 一个队伍是由谁创建的，及 队伍的信息
 */


create table team
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256)                       not null comment ' 队伍名称',
    description varchar(1024)                      null comment '描述 ',
    maxNum      int      default 1                 not null comment '最大人数 ',
    expireTime  datetime                           null comment '过期时间 ',
    userId      bigint comment ' 用户id（约定：用户id就是队长id）',
    status      int default 0 null comment ' 0 - 公开， 1 - 私有，2 - 加密',
    password    varchar(512)                       null comment '密码',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete    tinyint  default 0                 not null comment '是否删除'
)
    comment '队伍表';

-- 用户队伍关系表
/**
   这个表可以看到： 一个用户 加入了多少个队伍
                 一个队伍 里面 加入了 多少个用户
 */

create table user_team
(
    id            bigint auto_increment comment 'id'
        primary key,
    userId       bigint   comment '  用户id（约定：用户id就是队长id）',
    teamId  bigint      comment ' 队伍Id ',
    joinTime         datetime                           null comment '加入时间 ',
    createTime   datetime      default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime      default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint       default 0                 not null comment '是否删除',
)
    comment '用户队伍关系表';