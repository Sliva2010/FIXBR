package com.sliva2010.fixbr

/**
 * Manages system-level performance tweaks for game optimization.
 * Applies tweaks via root shell to CPU scheduling, GPU, and HWUI properties.
 */
object PerformanceTweaks {

    // Store original values for restoration
    private val originalValues = mutableMapOf<String, String>()

    /**
     * Applies performance tweaks for the specified game package.
     */
    fun applyTweaks(gamePackage: String): String {
        val commands = mutableListOf<String>()

        // 1. Renice game process to highest priority (-20)
        commands.add("pid=\$(pidof $gamePackage) && if [ -n \"\$pid\" ]; then renice -20 -p \$pid; fi")

        // 2. CPUSET - Move to performance cores (big cluster)
        commands.add("pid=\$(pidof $gamePackage) && if [ -n \"\$pid\" ]; then echo \$pid > /dev/cpuset/foreground/tasks 2>/dev/null; fi")
        commands.add("pid=\$(pidof $gamePackage) && if [ -n \"\$pid\" ]; then echo \$pid > /dev/cpuset/top-app/tasks 2>/dev/null; fi")

        // 3. SCHEDTUNE - Boost scheduling for game
        commands.add("pid=\$(pidof $gamePackage) && if [ -n \"\$pid\" ]; then echo 100 > /dev/stune/top-app/schedtune.boost 2>/dev/null; fi")

        // 4. HWUI Renderer tweaks for hardware acceleration
        commands.add("setprop debug.hwui.renderer skiagl")
        commands.add("setprop debug.hwui.use_buffer_age false")

        // 5. EGL hardware acceleration
        commands.add("setprop debug.egl.hw 1")

        // 6. SurfaceFlinger hardware rendering
        commands.add("setprop debug.sf.hw 1")

        // 7. Increase window manager events per second for smoother UI
        commands.add("setprop windowsmgr.max_events_per_sec 120")

        // 8. GPU rendering optimizations
        commands.add("setprop debug.hwui.render_dirty_regions false")

        // 9. CPU Governor - set to performance mode if available
        commands.add("if [ -f /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor ]; then echo performance > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor; fi")
        commands.add("if [ -f /sys/devices/system/cpu/cpu4/cpufreq/scaling_governor ]; then echo performance > /sys/devices/system/cpu/cpu4/cpufreq/scaling_governor; fi")

        // 10. Lock CPU max frequencies (if supported)
        commands.add("if [ -f /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq ]; then cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq; fi")
        commands.add("if [ -f /sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq ]; then cat /sys/devices/system/cpu/cpu4/cpufreq/cpuinfo_max_freq > /sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq; fi")

        // 11. GPU max frequency lock (Adreno)
        commands.add("if [ -f /sys/class/kgsl/kgsl-3d0/max_gpuclk ]; then cat /sys/class/kgsl/kgsl-3d0/max_clock_mhz > /sys/class/kgsl/kgsl-3d0/devfreq/max_freq 2>/dev/null; fi")
        commands.add("if [ -f /sys/class/kgsl/kgsl-3d0/devfreq/governor ]; then echo performance > /sys/class/kgsl/kgsl-3d0/devfreq/governor 2>/dev/null; fi")

        // 12. I/O scheduler tweaks
        commands.add("if [ -f /sys/block/mmcblk0/queue/scheduler ]; then echo noop > /sys/block/mmcblk0/queue/scheduler 2>/dev/null; fi")
        commands.add("if [ -f /sys/block/sda/queue/scheduler ]; then echo noop > /sys/block/sda/queue/scheduler 2>/dev/null; fi")

        // 13. VM tweaks for better performance
        commands.add("echo 3 > /proc/sys/vm/drop_caches 2>/dev/null")
        commands.add("echo 20 > /proc/sys/vm/dirty_ratio 2>/dev/null")
        commands.add("echo 10 > /proc/sys/vm/dirty_background_ratio 2>/dev/null")

        // Execute all commands
        return RootShell.execCommands(commands)
    }

    /**
     * Restores original system settings.
     */
    fun restoreDefaults(gamePackage: String): String {
        val commands = mutableListOf<String>()

        // 1. Reset nice value to default (0)
        commands.add("pid=\$(pidof $gamePackage) && if [ -n \"\$pid\" ]; then renice 0 -p \$pid; fi")

        // 2. Reset CPUSET to default
        commands.add("pid=\$(pidof $gamePackage) && if [ -n \"\$pid\" ]; then echo \$pid > /dev/cpuset/background/tasks 2>/dev/null; fi")

        // 3. Reset HWUI properties
        commands.add("setprop debug.hwui.renderer skiagl")
        commands.add("setprop debug.hwui.use_buffer_age true")

        // 4. Reset EGL
        commands.add("setprop debug.egl.hw 1")

        // 5. Reset SurfaceFlinger
        commands.add("setprop debug.sf.hw 1")

        // 6. Reset window manager
        commands.add("setprop windowsmgr.max_events_per_sec 60")

        // 7. Reset GPU rendering
        commands.add("setprop debug.hwui.render_dirty_regions true")

        // 8. Reset CPU Governor to schedutil/interactive
        commands.add("if [ -f /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor ]; then echo schedutil > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor; fi")
        commands.add("if [ -f /sys/devices/system/cpu/cpu4/cpufreq/scaling_governor ]; then echo schedutil > /sys/devices/system/cpu/cpu4/cpufreq/scaling_governor; fi")

        // 9. Reset GPU governor
        commands.add("if [ -f /sys/class/kgsl/kgsl-3d0/devfreq/governor ]; then echo msm-adreno-tz > /sys/class/kgsl/kgsl-3d0/devfreq/governor 2>/dev/null; fi")

        // 10. Reset I/O scheduler
        commands.add("if [ -f /sys/block/mmcblk0/queue/scheduler ]; then echo cfq > /sys/block/mmcblk0/queue/scheduler 2>/dev/null; fi")
        commands.add("if [ -f /sys/block/sda/queue/scheduler ]; then echo mq-deadline > /sys/block/sda/queue/scheduler 2>/dev/null; fi")

        // Execute all commands
        return RootShell.execCommands(commands)
    }
}
