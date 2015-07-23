package ch.qos.logback.core.rolling;

import ch.qos.logback.core.joran.spi.NoAutoStart;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

@NoAutoStart
public class StartupSizeBasedTriggeringPolicy<E> extends SizeAndTimeBasedFNATP<E> {

    private final AtomicReference<Boolean> isFirstTime = new AtomicReference<>(true);

    @Override
    public boolean isTriggeringEvent(File activeFile, final E event) {
        boolean result = super.isTriggeringEvent(activeFile, event);
        boolean isStartup = isFirstTime.compareAndSet(true, false);
        if (isStartup) {
            elapsedPeriodsFileName = tbrp.fileNamePatternWCS
                    .convertMultipleArguments(dateInCurrentPeriod, currentPeriodsCounter);
            currentPeriodsCounter++;
            return true;
        }

        return result;
    }

}
