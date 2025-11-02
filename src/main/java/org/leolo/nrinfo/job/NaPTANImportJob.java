package org.leolo.nrinfo.job;

import org.leolo.nrinfo.dao.NaptanDao;
import org.leolo.nrinfo.model.Naptan;
import org.leolo.nrinfo.model.Nptg;
import org.leolo.nrinfo.service.ConfigurationService;
import org.leolo.nrinfo.service.JobService;
import org.leolo.nrinfo.util.GeoReferenceUtil;
import org.leolo.nrinfo.util.HttpRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.Tuple2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

@Component
@Scope("prototype")
public class NaPTANImportJob extends AbstractJob {

    private Logger logger = LoggerFactory.getLogger(NaPTANImportJob.class);

    public static final String NPTG_URL   = "https://beta-naptan.dft.gov.uk/Download/Nptg/csv";
    public static final String NAPTAN_URL = "https://beta-naptan.dft.gov.uk/Download/National/csv";

    @Autowired private JobService jobService;
    @Autowired private ConfigurationService configurationService;
    @Autowired private NaptanDao naptanDao;

    public NaPTANImportJob() {
        super(NaPTANImportJob.class.getName());
    }

    @Override public void run() {
        logger.info("NaPTANImportJob started");
        jobService.writeMessage(this, "WARNING : NO BACKUP OF OLD TABLE. ENTRY WILL BE UPSERTED");
        long startTime = System.currentTimeMillis();
        loadNPTG();
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        logger.info("Loaded NPTG Locality in {} ms", elapsedTime);
        jobService.writeMessage(this, "NPTG Locality - Done in "+elapsedTime+" ms");
        startTime = System.currentTimeMillis();
        loadNaPTAN();
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        logger.info("Loaded NaPTAN stop point in {} ms", elapsedTime);
        jobService.writeMessage(this, "NaPTAN stop point - Done in "+elapsedTime+" ms");
    }

    private void loadNPTG() {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(HttpRequestUtil.sendSimpleRequestAsStream(NPTG_URL)))) {
            //1st line is CSV header and should be discarded
            String line = br.readLine();
            final int BATCH_SIZE = Integer.parseInt(configurationService.getConfiguration("job.naptan.batch_size","100"));
            logger.info("NPTG Batch Size: {}", BATCH_SIZE);
            ArrayList<Nptg> nptgs = new ArrayList<>(BATCH_SIZE);
            while (line != null) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                String[] tokens = line.split(",");
                Nptg nptg = new Nptg();
                nptg.setNptgCode(tokens[0]);
                nptg.setLocalityName(tokens[1]);
                nptg.setShortName(tokens[3]);
                nptg.setQualifierName(tokens[5]);
                nptg.setAdminAreaCode(tokens[11]);
                nptg.setDistrictCode(tokens[12]);
                nptg.setGridCode(tokens[14]);
                nptg.setEasting(parseInt(tokens[15]));
                nptg.setNorthing(parseInt(tokens[16]));
                if(!(nptg.getEasting()==0 && nptg.getNorthing()==0)) {
                    Tuple2<Double, Double> wgs84 = GeoReferenceUtil.convertUKOStoWGS84(nptg.getEasting(), nptg.getNorthing());
                    nptg.setLongitude(wgs84._1);
                    nptg.setLatitude(wgs84._2);
                }
                try {
                    nptg.setUpdatedDate(tokens[18]);
                } catch (ParseException e) {
                    logger.error("Unable to parse date {} - {}", tokens[18], e.getMessage());
                    continue;
                }
                nptg.setRevisionNumber(Integer.parseInt(tokens[19]));
                nptgs.add(nptg);
                if (nptgs.size() >= BATCH_SIZE) {
                    naptanDao.upsertNptg(nptgs);
                    nptgs.clear();
                }
            }
            naptanDao.upsertNptg(nptgs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
        }

    }

    private void loadNaPTAN() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(HttpRequestUtil.sendSimpleRequestAsStream(NAPTAN_URL)))) {
            //1st line is CSV header and should be discarded
            String line = br.readLine();
            final int BATCH_SIZE = Integer.parseInt(configurationService.getConfiguration("job.naptan.batch_size", "100"));
            logger.info("NaPTAN Batch Size: {}", BATCH_SIZE);
            ArrayList<Naptan> naptans = new ArrayList<>(BATCH_SIZE);
            while (line != null) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                String[] tokens = line.split(",");
                Naptan naptan = new Naptan();
                naptan.setAtocCode(tokens[0]);
                naptan.setNaptanCode(tokens[1]);
                naptan.setPlateCode(tokens[2]);
                naptan.setCleardownCode(tokens[3]);
                naptan.setCommonName(tokens[4]);
                naptan.setShortCommonName(tokens[6]);
                naptan.setLandmark(tokens[8]);
                naptan.setStreet(tokens[10]);
                naptan.setCrossing(tokens[12]);
                naptan.setIndicator(tokens[14]);
                naptan.setBearing(tokens[16]);
                naptan.setNptgCode(tokens[17]);
                naptan.setTown(tokens[21]);
                naptan.setSuburb(tokens[23]);
                naptan.setCountry(null);
                naptan.setLocalityCentre(tokens[25]);
                naptan.setGridType(tokens[26]);
                naptan.setEasting(parseInt(tokens[27]));
                naptan.setNorthing(parseInt(tokens[28]));
                naptan.setLongitude(parseDouble(tokens[29]));
                naptan.setLatitude(parseDouble(tokens[30]));
                naptan.setStopType(tokens[31]);
                naptan.setBusStopType(tokens[32]);
                try {
                    naptan.setUpdatedDate(tokens[39]);
                } catch (ParseException e) {
                    logger.error("Unable to parse date {} - {}", tokens[39], e.getMessage());
                    continue;
                }
                naptan.setRevisionNumber(parseInt(tokens[40]));
                naptans.add(naptan);
                if (naptans.size() >= BATCH_SIZE) {
                    naptanDao.upsertNaptan(naptans);
                    naptans.clear();
                }
            }
            naptanDao.upsertNaptan(naptans);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private double parseDouble(String token) {
        if (token == null || token.isEmpty()) {
            return 0;
        }
        return Double.parseDouble(preprocessString(token));
    }

    private int parseInt(String token) {
        if (token == null || token.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(preprocessString(token));
    }

    private String preprocessString(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        token = token.replace("\"","");
        return token.strip();
    }
}
