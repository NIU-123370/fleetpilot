#!/usr/bin/env python3
# SPDX-License-Identifier: Apache-2.0
# 出处核对：`diff -rq` 出来的差异，必须和 tools/provenance-changed.txt 里申报的那一份完全一致。
#
# 为什么要机器判：UPSTREAM.md 承诺"和上游 diff 出来只有表里那些文件，多出来任何一条说明有人
# 在没更新本表的情况下动了代码"。这句话以前只是印在纸上——CI 里那个 job 只打印不判定。
# 现在它判定：改了代码没申报，红；申报了却没改（比如把某处改回去了），也红。
#
# 用法:
#   tools/provenance_check.py /tmp/upstream .            # 核对
#   tools/provenance_check.py /tmp/upstream . --write    # 把当前差异重新写进申报文件
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
LIST_FILE = os.path.join(HERE, "provenance-changed.txt")
PINNED_UPSTREAM_SHA = "0b57e35d4e4c8460fdd87fcd33f751f764e7423d"

# 只在核对时忽略的东西：构建产物、IDE/Gradle 缓存。注意这是"比对时忽略",
# 不是给代码改动开的白名单——真改了源码一定要申报。
SKIP = re.compile(r"(^|/)(build|\.gradle|\.kotlin|\.idea|\.git)(/|$)")


def changed_paths(upstream: str, ours: str) -> set[str]:
    """`diff -rq` 的输出压成相对路径集合。上游目录里没有的同名文件也算差异。"""
    out = subprocess.run(
        ["diff", "-rq", "-x", ".git", upstream, ours],
        capture_output=True, text=True, cwd=ours,
    ).stdout
    found: set[str] = set()
    for line in out.splitlines():
        m = re.match(r"Files .* and \./(.*) differ$", line)
        if m:
            found.add(m.group(1))
            continue
        m = re.match(r"Only in \.(.*): (.*)$", line)
        if m:
            sub, name = m.group(1).strip("/"), m.group(2)
            found.add(f"{sub}/{name}" if sub else name)
    return {p for p in found if not SKIP.search(p)}


def declared() -> set[str]:
    with open(LIST_FILE, encoding="utf-8") as f:
        return {l.strip() for l in f if l.strip() and not l.startswith("#")}


def write_declared(paths: set[str]) -> None:
    header = (
        f"# 与上游 {PINNED_UPSTREAM_SHA[:7]} 相比，本仓库改过或新增的文件（tools/provenance_check.py 生成）。\n"
        "# 逐文件说明在 UPSTREAM.md 的「本仓库相对上游改了什么」那张表里。\n"
        "# 改代码就同步这里：tools/provenance_check.py <上游目录> . --write\n"
    )
    with open(LIST_FILE, "w", encoding="utf-8") as f:
        f.write(header + "".join(p + "\n" for p in sorted(paths)))


def main(argv: list[str]) -> int:
    args = [a for a in argv[1:] if a != "--write"]
    if len(args) != 2:
        print(__doc__)
        return 2
    upstream, ours = args
    now = changed_paths(upstream, ours)
    if "--write" in argv:
        write_declared(now)
        print(f"已写入申报文件：{len(now)} 条 → {LIST_FILE}")
        return 0
    want = declared()
    missing = sorted(now - want)   # 动了代码没申报
    stale = sorted(want - now)     # 申报了但实际没改
    print(f"比对：上游 {os.path.basename(upstream)}，本仓库 {ours}；实际差异 {len(now)} 条，申报 {len(want)} 条")
    for label, items in (("改了但没申报", missing), ("申报了却已不存在差异", stale)):
        for p in items:
            print(f"{label}: {p}")
    if missing or stale:
        one_line = "; ".join(f"{label} {len(items)}" for label, items in (("没申报", missing), ("已陈旧", stale)) if items)
        print(f"::error::出处核对不一致：{one_line}（逐条在上面）。更新：tools/provenance_check.py /tmp/upstream . --write")
        return 1
    print("一致：代码差异与申报完全对得上")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
