我们需要一个DB中所有表的结构和字段定义，约束字符串等，用于其他地方
首先要做的是用mysqldump 导出DB表结构:mysqldump -hx.x.x.x -uxx -pxx -d db_xx --skip-lock-tables >xx.sql
这份文件使用工具解析出我们需要的内容（分库分表默认是16个，可以修改成其他，用来区分）。
结果如下：
    t_1 = { 
        ["TB"] = "t_xxx1", 
        ["DB"] = "db_xxxx", 
        ["fields"] = {"id","t1","t2","t3","t4","t5","t6","t7","t8","t9","t10","t11","t12",}, 
        ["src_2_dst_map"] = {db = "db_xxx2", tb = "t_xxx23",},
        ["use_id_limit"] = true, 
        ["keys"] = {"id",},  },
    t_2 = { 
        ["TB"] = "t_xxxx2", 
        ["DB"] = "db_xxxxxx", 
        ["fields"] = {"id","t1","t2","t3","t4","t5","t6","t7","t8","t9","t10","t11","t12","t13","t14"}, 
        ["src_2_dst_map"] = {db = "db_xxxx22", tb = "t_xxxxxxx3",},
        ["use_id_limit"] = true, 
        ["keys"] = {"id",},  },
      
      ......
