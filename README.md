# SOA
my soa
自己写一套基于netty+protobuf+redis的游戏架构练练手

client是客户端的程序，只是简单的模拟一下客户端给服务器收发协议的情况

gate负责直联client和游戏逻辑服务器，作为中间网关服务器

rgame是游戏逻辑服务器，负责处理游戏主逻辑相关的各种东西

还有很多东西没弄完
