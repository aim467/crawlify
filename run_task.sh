#!/bin/bash
# run_task.sh
# 用法: run_task.sh "<script_cmd>" <log_file> <pid_file> <exit_file>

script_cmd="$1"
log_file="$2"
pid_file="$3"
exit_file="$4"

# 后台运行命令，stderr/stdout 都重定向到 log_file
nohup bash -c "$script_cmd" >"$log_file" 2>&1 &
pid=$!

# 写 PID 到 pid_file
echo $pid > "$pid_file"

# 等待任务完成，并记录退出码
wait $pid
exit_code=$?
echo $exit_code > "$exit_file"

# 删除 pid_file（避免 PID 复用问题）
rm -f "$pid_file"