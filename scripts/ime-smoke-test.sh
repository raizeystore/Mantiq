#!/usr/bin/env bash
set -euo pipefail

apk_path="${1:-app/build/outputs/apk/debug/app-debug.apk}"
package_name="com.raizey.mantiq"
activity_component="${package_name}/.MainActivity"
ime_component="${package_name}/.ime.MantiqImeService"
ime_component_full="${package_name}/${package_name}.ime.MantiqImeService"

if [[ ! -f "${apk_path}" ]]; then
  echo "APK not found: ${apk_path}" >&2
  exit 1
fi

adb install -r "${apk_path}"
adb logcat -c || true

collect_diagnostics() {
  adb exec-out screencap -p > /tmp/mantiq-smoke.png 2>/dev/null || true
  adb logcat -d > /tmp/mantiq-logcat.txt 2>/dev/null || true
  adb shell dumpsys input_method > /tmp/mantiq-input-method.txt 2>/dev/null || true
  adb shell dumpsys package "${package_name}" > /tmp/mantiq-package.txt 2>/dev/null || true
  adb shell dumpsys window > /tmp/mantiq-window-state.txt 2>/dev/null || true
  adb shell uiautomator dump /sdcard/mantiq-window.xml >/dev/null 2>&1 || true
  adb pull /sdcard/mantiq-window.xml /tmp/mantiq-window.xml >/dev/null 2>&1 || true
}
trap collect_diagnostics EXIT

launch_output="$(adb shell am start -W -n "${activity_component}")"
echo "${launch_output}"
grep -q "Status: ok" <<<"${launch_output}"
sleep 2

if ! adb shell pidof "${package_name}" >/dev/null; then
  echo "Mantiq process did not remain alive after launch" >&2
  exit 1
fi

adb shell dumpsys package "${package_name}" > /tmp/mantiq-package.txt
grep -q "MantiqImeService" /tmp/mantiq-package.txt

adb shell ime list -a > /tmp/mantiq-ime-list.txt
grep -q "${ime_component}" /tmp/mantiq-ime-list.txt

adb shell ime enable "${ime_component}"
adb shell ime set "${ime_component}"
adb shell settings put secure show_ime_with_hard_keyboard 1

selected_ime="$(adb shell settings get secure default_input_method | tr -d '\r')"
if [[ "${selected_ime}" != "${ime_component}" && "${selected_ime}" != "${ime_component_full}" ]]; then
  echo "Unexpected selected IME: ${selected_ime}" >&2
  exit 1
fi

test_field_node=""
for _ in 1 2 3 4 5; do
  adb shell uiautomator dump /sdcard/mantiq-window.xml >/dev/null
  adb pull /sdcard/mantiq-window.xml /tmp/mantiq-window.xml >/dev/null
  test_field_node="$(grep -o '<node[^>]*resource-id="com.raizey.mantiq:id/keyboard_test_field"[^>]*>' /tmp/mantiq-window.xml | head -1 || true)"
  if [[ -n "${test_field_node}" ]]; then
    break
  fi
  adb shell input swipe 160 540 160 150 300
  sleep 1
done

test_field_bounds="$(sed -n 's/.*bounds="\[\([0-9]*\),\([0-9]*\)\]\[\([0-9]*\),\([0-9]*\)\]".*/\1 \2 \3 \4/p' <<<"${test_field_node}")"
if [[ -z "${test_field_bounds}" ]]; then
  echo "Could not locate the Mantiq test field" >&2
  exit 1
fi
read -r left top right bottom <<<"${test_field_bounds}"
tap_x=$(((left + right) / 2))
tap_y=$(((top + bottom) / 2))
adb shell input tap "${tap_x}" "${tap_y}"
sleep 3

adb shell dumpsys input_method > /tmp/mantiq-input-method.txt
grep -q "MantiqImeService" /tmp/mantiq-input-method.txt
if ! grep -Eq "mInputShown=true|mInputViewShown=true|isInputViewShown=true" /tmp/mantiq-input-method.txt; then
  echo "Mantiq is selected but its input view is not shown" >&2
  cat /tmp/mantiq-input-method.txt >&2
  exit 1
fi

adb shell uiautomator dump /sdcard/mantiq-window.xml >/dev/null
adb pull /sdcard/mantiq-window.xml /tmp/mantiq-window.xml >/dev/null
grep -q "Mantiq" /tmp/mantiq-window.xml

collect_diagnostics
if grep -A 15 -B 2 "FATAL EXCEPTION" /tmp/mantiq-logcat.txt | grep -q "${package_name}"; then
  echo "Mantiq crashed during the smoke test" >&2
  grep -A 30 -B 2 "FATAL EXCEPTION" /tmp/mantiq-logcat.txt >&2
  exit 1
fi

echo "Mantiq IME smoke test passed"
