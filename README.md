我们需要一个DB中所有表的结构和字段定义，约束字符串等，用于其他地方
首先要做的是用mysqldump 导出DB表结构:mysqldump -hx.x.x.x -uxx -pxx -d db_xx --skip-lock-tables >xx.sql
这份文件使用工具解析出我们需要的内容。