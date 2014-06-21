package harlequinmettle.finance.technicalanalysis.model.db;

public interface TechnicalDatabaseInterface {
public float[][] queryTechnicalDatabase(String ticker);

public static final int DATE = 0;
public static final int OPEN = 1;
public static final int HIGH = 2;
public static final int LOW = 3;
public static final int CLOSE = 4;
public static final int VOLUME = 5;
public static final int ADJCLOSE = 6;

public static final String d = "date";
public static final String o = "open";
public static final String h = "high";
public static final String l = "low";
public static final String c = "close";
public static final String v = "vol";
public static final String a = "adjcls";
public static final String[] elements = { d, o, h, l, c, v, a };
}
