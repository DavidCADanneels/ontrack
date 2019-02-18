package net.nemerosa.ontrack.service.support;

import io.micrometer.core.instrument.MeterRegistry;
import net.nemerosa.ontrack.common.Time;
import net.nemerosa.ontrack.model.security.Account;
import net.nemerosa.ontrack.model.security.ApplicationManagement;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.support.ApplicationLogEntry;
import net.nemerosa.ontrack.model.support.ApplicationLogEntryFilter;
import net.nemerosa.ontrack.model.support.ApplicationLogService;
import net.nemerosa.ontrack.model.support.Page;
import net.nemerosa.ontrack.repository.ApplicationLogEntriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApplicationLogServiceImpl implements ApplicationLogService {

    private final Logger logger = LoggerFactory.getLogger(ApplicationLogService.class);

    private final SecurityService securityService;
    private final ApplicationLogEntriesRepository entriesRepository;
    private final MeterRegistry meterRegistry;

    @Autowired
    public ApplicationLogServiceImpl(SecurityService securityService, ApplicationLogEntriesRepository entriesRepository, MeterRegistry meterRegistry) {
        this.securityService = securityService;
        this.entriesRepository = entriesRepository;
        this.meterRegistry = meterRegistry;
    }

    @Override
    @Transactional(propagation = Propagation.NESTED)
    public void log(ApplicationLogEntry entry) {
        ApplicationLogEntry signedEntry = entry.withAuthentication(
                securityService.getAccount().map(Account::getName).orElse("anonymous")
        );
        doLog(signedEntry);
    }

    @Override
    public void cleanup(int retentionDays) {
        entriesRepository.cleanup(retentionDays);
    }

    @Override
    public void deleteLogEntries() {
        entriesRepository.deleteLogEntries();
    }

    private synchronized void doLog(ApplicationLogEntry entry) {
        // Logging
        logger.error(
                String.format(
                        "[%s] name=%s,authentication=%s,timestamp=%s,%s%nStacktrace: %s",
                        entry.getLevel(),
                        entry.getType().getName(),
                        entry.getAuthentication(),
                        Time.forStorage(entry.getTimestamp()),
                        entry.getDetailList().stream()
                                .map(nd -> String.format("%s=%s", nd.getName(), nd.getDescription()))
                                .collect(Collectors.joining(",")),
                        entry.getStacktrace()
                )
        );
        // Storing in database
        entriesRepository.log(entry);
        // Metrics
        meterRegistry.counter(
                "ontrack_error",
                "type", entry.getType().getName()
        ).increment();
    }

    @Override
    public synchronized int getLogEntriesTotal() {
        return entriesRepository.getTotalCount();
    }

    @Override
    public synchronized List<ApplicationLogEntry> getLogEntries(ApplicationLogEntryFilter filter, Page page) {
        securityService.checkGlobalFunction(ApplicationManagement.class);
        return entriesRepository.getLogEntries(filter, page);
    }
}
