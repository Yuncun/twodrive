#!/usr/bin/env bash
# Drive TwoDrive prod sign-in on the Pixel and approve it in Authenticator on the same phone.
D="${TWODRIVE_DEVICE_SERIAL:-44271JEKB17967}"; S=$(dirname "$0"); PW="$1"
T(){ python3 $S/tap.py $D "$1"; }
texts(){ adb -s $D shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb -s $D shell cat /sdcard/ui.xml | tr '>' '\n' | grep -oE 'text="[^"]{1,90}"' | sort -u; }
has(){ texts | grep -q "text=\"$1\""; }
unlock(){ if adb -s $D shell dumpsys window | grep -q BiometricPrompt; then T "Use password"; sleep 2; adb -s $D shell input text "$PW"; adb -s $D shell input keyevent ENTER; sleep 4; fi; }
adb -s $D shell am force-stop codes.fixmy.twodrive.debug; adb -s $D shell am force-stop com.android.chrome; sleep 1
adb -s $D shell monkey -p codes.fixmy.twodrive.debug -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 4
T "Sign in" || exit 1; sleep 8
if has "Choose an account"; then T "yuncuns@gmail.com"; sleep 8; fi
if has "Email or phone number"; then T "Email or phone number"; sleep 2; T "yuncuns@gmail.com" || adb -s $D shell input text "yuncuns@gmail.com"; sleep 1; T "Next"; sleep 8; fi
adb -s $D shell dumpsys window | grep -q CredentialChooser && { adb -s $D shell input keyevent BACK; sleep 4; }
has "Approve sign-in with a mobile app" && { T "Approve sign-in with a mobile app"; sleep 6; }
has "Send another request" && { T "Send another request"; sleep 6; }
N=$(texts | grep -oE 'text="[0-9]{2}"' | head -1 | grep -oE '[0-9]+'); echo "number=$N"; [ -n "$N" ] || { echo "no number; screen:"; texts | head -15; exit 1; }
adb -s $D shell monkey -p com.azure.authenticator -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 5; unlock
for i in 1 2 3; do has "Enter number here" && break; sleep 3; done
T "Enter number here" || { echo "no approval prompt"; texts | head -12; exit 1; }
sleep 1; adb -s $D shell input text "$N"; sleep 1; T "APPROVE"; sleep 3; unlock
echo "approved; waiting for redirect"; sleep 12
adb -s $D shell dumpsys window | grep -m1 mCurrentFocus
adb -s $D shell am start -n codes.fixmy.twodrive.debug/codes.fixmy.twodrive.MainActivity >/dev/null 2>&1; sleep 5
adb -s $D shell dumpsys window | grep -m1 mCurrentFocus; texts | head -25
