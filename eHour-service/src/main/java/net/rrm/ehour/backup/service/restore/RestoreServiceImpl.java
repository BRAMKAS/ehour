package net.rrm.ehour.backup.service.restore;

import net.rrm.ehour.backup.domain.ParseSession;
import net.rrm.ehour.backup.service.DatabaseTruncater;
import net.rrm.ehour.backup.service.backup.BackupEntityLocator;
import net.rrm.ehour.config.EhourConfig;
import net.rrm.ehour.persistence.config.dao.ConfigurationDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.XMLEventReader;

/**
 * @author thies (Thies Edeling - thies@te-con.nl)
 *         Created on: Nov 13, 2010 - 5:34:24 PM
 */
@Service("importService")
public class RestoreServiceImpl implements RestoreService {
    private static final Logger LOG = Logger.getLogger(RestoreServiceImpl.class);

    private ConfigurationDao configurationDao;

    private ConfigurationParserDao configurationParserDao;

    private DomainObjectParserDao domainObjectParserDao;

    private UserRoleParserDao userRoleParserDao;

    private DatabaseTruncater databaseTruncater;

    private BackupEntityLocator backupEntityLocator;

    private EhourConfig ehourConfig;

    @Autowired
    public RestoreServiceImpl(ConfigurationDao configurationDao, ConfigurationParserDao configurationParserDao, DomainObjectParserDao domainObjectParserDao, UserRoleParserDao userRoleParserDao, DatabaseTruncater databaseTruncater, EhourConfig ehourConfig, BackupEntityLocator backupEntityLocator) {
        this.configurationDao = configurationDao;
        this.configurationParserDao = configurationParserDao;
        this.domainObjectParserDao = domainObjectParserDao;
        this.userRoleParserDao = userRoleParserDao;
        this.databaseTruncater = databaseTruncater;
        this.ehourConfig = ehourConfig;
        this.backupEntityLocator = backupEntityLocator;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ParseSession importDatabase(ParseSession session) {
        try {
            if (!ehourConfig.isInDemoMode()) {
                databaseTruncater.truncateDatabase();

                session.clearSession();

                XMLEventReader xmlEventReader = BackupFileUtil.createXmlReaderFromFile(session.getFilename());

                XmlParser parser = new XmlParserBuilder()
                        .setConfigurationDao(configurationDao)
                        .setConfigurationParserDao(configurationParserDao)
                        .setDomainObjectParserDao(domainObjectParserDao)
                        .setUserRoleParserDao(userRoleParserDao)
                        .setXmlReader(xmlEventReader)
                        .setSkipValidation(true)
                        .setBackupEntityLocator(backupEntityLocator)
                        .build();

                parser.parseXml(session, xmlEventReader);
            }
        } catch (Exception e) {
            session.setGlobalError(true);
            session.setGlobalErrorMessage(e.getMessage());
            LOG.error(e.getMessage(), e);
        } finally {
            session.deleteFile();
            session.setImported(true);
        }

        return session;
    }

    @Override
    public ParseSession prepareImportDatabase(String xmlData) {
        ParseSession session;

        try {
            String tempFilename = BackupFileUtil.writeToTempFile(xmlData);
            session = validateXml(xmlData);
            session.setFilename(tempFilename);
        } catch (Exception e) {
            session = new ParseSession();
            session.setGlobalError(true);
            session.setGlobalErrorMessage(e.getMessage());
            LOG.error(e.getMessage(), e);
        }

        return session;
    }

    private ParseSession validateXml(String xmlData) throws Exception {
        ParseSession status = new ParseSession();

        XMLEventReader eventReader = BackupFileUtil.createXmlReader(xmlData);

        DomainObjectParserDaoValidatorImpl domainObjectParserDaoValidator = new DomainObjectParserDaoValidatorImpl();
        ConfigurationParserDaoValidatorImpl configurationParserDaoValidator = new ConfigurationParserDaoValidatorImpl();
        UserRoleParserDaoValidatorImpl userRoleParserDaoValidator = new UserRoleParserDaoValidatorImpl();

        XmlParser importer = new XmlParserBuilder()
                .setConfigurationDao(configurationDao)
                .setConfigurationParserDao(configurationParserDaoValidator)
                .setDomainObjectParserDao(domainObjectParserDaoValidator)
                .setUserRoleParserDao(userRoleParserDaoValidator)
                .setBackupEntityLocator(backupEntityLocator)
                .setXmlReader(eventReader)
                .build();

        importer.parseXml(status, eventReader);

        return status;
    }

    public void setConfigurationDao(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    public void setDatabaseTruncater(DatabaseTruncater databaseTruncater) {
        this.databaseTruncater = databaseTruncater;
    }
}
