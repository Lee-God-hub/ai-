# -*- coding: utf-8 -*-
"""
自动调度爬虫：每 60 秒执行一次，每次从 20 个随机城市各爬取 20 条房源（共 400 条）

用法：
    python auto_crawler.py          # 默认：每60秒、20城市、每城市20条、随机交易类型
    python auto_crawler.py 30 10 5  # 自定义：每30秒、10城市、每城市5条、随机交易类型
    python auto_crawler.py 30 10 5 0  # 指定只爬出租
    python auto_crawler.py 30 10 5 1  # 指定只爬出售
    python auto_crawler.py 30 10 5 2  # 指定爬取全部（出租+出售）
    停止：按 Ctrl+C
"""
import subprocess
import sys
import time
import random
from datetime import datetime


def run_once(city_count, per_city_count, transaction_type=2, script_path='anjuke_spider.py', timeout_seconds=900):
    """执行一次爬虫任务。成功返回 True，失败返回 False。"""
    total = city_count * per_city_count
    cmd = [sys.executable, script_path, str(total), str(city_count), str(transaction_type)]
    
    type_desc = {0: '出租', 1: '出售', 2: '随机(出租+出售)'}
    print('\n' + '=' * 70)
    print('[调度 %s] 启动爬虫: %d 城市 × %d 条 = %d 条，交易类型: %s，超时 %d 秒' % (
        datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        city_count, per_city_count, total, type_desc.get(transaction_type, '未知'), timeout_seconds,
    ))
    print('  命令: %s' % ' '.join(cmd))
    print('=' * 70)

    start = time.time()
    try:
        result = subprocess.run(cmd, capture_output=False, timeout=timeout_seconds)
        elapsed = time.time() - start
        ok = result.returncode == 0
        status = '✓ 成功' if ok else '✗ 失败(退出码 %d)' % result.returncode
        print('\n[%s] 本轮结束: %s，耗时 %.1f 秒' % (
            datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
            status, elapsed,
        ))
        return ok
    except subprocess.TimeoutExpired:
        elapsed = time.time() - start
        print('\n[警告] 本轮爬虫超时（%.1f 秒 > %d 秒），强制终止，等待下一轮' % (
            elapsed, timeout_seconds,
        ))
        return False
    except Exception as e:
        print('\n[错误] 本轮爬虫异常: %s' % e)
        return False


def main():
    # ---------- 参数解析 ----------
    # 用法：python auto_crawler.py [间隔秒数] [城市数] [每城市条数] [交易类型]
    # 交易类型: 0=出租, 1=出售, 2=随机(默认)
    args = sys.argv[1:]
    interval_seconds = 60            # 默认：每 60 秒一次
    city_count = 20                  # 默认：20 个随机城市
    per_city_count = 20              # 默认：每城市 20 条
    transaction_type = 2             # 默认：随机交易类型（出租+出售）

    if len(args) >= 1 and args[0].isdigit():
        interval_seconds = int(args[0])
    if len(args) >= 2 and args[1].isdigit():
        city_count = int(args[1])
    if len(args) >= 3 and args[2].isdigit():
        per_city_count = int(args[2])
    if len(args) >= 4 and args[3].isdigit():
        transaction_type = int(args[3])
        if transaction_type not in [0, 1, 2]:
            print('[警告] 无效的交易类型参数，将使用默认值(随机)')
            transaction_type = 2

    total_per_round = city_count * per_city_count

    type_desc = {0: '只爬出租', 1: '只爬出售', 2: '随机(出租+出售)'}
    print('=' * 70)
    print(' 安居客房源爬虫 · 自动调度器')
    print('=' * 70)
    print('  执行间隔: 每 %d 秒一次' % interval_seconds)
    print('  每次城市数: %d 个随机城市' % city_count)
    print('  每城市条数: %d 条' % per_city_count)
    print('  每次总数: %d 条' % total_per_round)
    print('  交易类型: %s' % type_desc.get(transaction_type, '未知'))
    print('  停止方法: 按 Ctrl+C')
    print('=' * 70)
    print()

    if interval_seconds < 10:
        print('[警告] 间隔时间过短（<10秒），可能触发安居客反爬。建议 ≥ 30 秒。')
        print()

    # ---------- 主循环 ----------
    round_num = 0
    try:
        while True:
            round_num += 1
            print('\n>>> 第 %d 轮开始 (%s)' % (
                round_num,
                datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
            ))

            # 确定本轮的交易类型
            if transaction_type == 2:
                # 随机选择：0=出租, 1=出售
                current_type = random.choice([0, 1])
            else:
                current_type = transaction_type

            # 执行一次爬虫
            run_once(city_count, per_city_count, current_type)

            # 等待下一轮
            print('\n[等待] %d 秒后执行下一轮（按 Ctrl+C 停止）...' % interval_seconds)
            time.sleep(interval_seconds)

    except KeyboardInterrupt:
        print('\n\n' + '=' * 70)
        print('[停止] 收到 Ctrl+C，自动调度已停止。')
        print('       共执行 %d 轮。' % round_num)
        print('=' * 70)
    except Exception as e:
        print('\n\n[严重错误] 调度器异常: %s' % e)


if __name__ == '__main__':
    main()
