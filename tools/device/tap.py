import re,subprocess,sys
d,label=sys.argv[1],sys.argv[2]
subprocess.run(['adb','-s',d,'shell','uiautomator','dump','/sdcard/ui.xml'],capture_output=True)
x=subprocess.run(['adb','-s',d,'shell','cat','/sdcard/ui.xml'],capture_output=True,text=True).stdout
for node in re.findall(r'<node [^>]*>',x):
    if f'text="{label}"' in node or f'content-desc="{label}"' in node:
        a=list(map(int,re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"',node).groups()))
        cx,cy=(a[0]+a[2])//2,(a[1]+a[3])//2; subprocess.run(['adb','-s',d,'shell','input','tap',str(cx),str(cy)]); print('tapped',label,cx,cy); sys.exit(0)
print('NOT FOUND',label); sys.exit(1)
