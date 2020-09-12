How to compile the code:

<code>
bmake depend
bmake
bmake install
</code>

in root

<code>bmake && bmake install</code>

in kern/complile/ASST[1|2|3]

<code>sys161 kernel</code>

to run the system

CONTENT:
1. asst0 is about how to debug the sys161 where running on os161
2. asst1 is about arrange lock properly
3. asst2 is about write systemcall and file table
4. asst3 is about vm(virtual memory)

NOTE:
1. remember to config the sys161.conf properly
2. To init gdb, create ``~/.gdbinit`` and mention ``set auto-load safe-path``, mv ``.gdbinit`` to cs3231/root and modify it.

