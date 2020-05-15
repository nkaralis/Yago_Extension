package gr.uoa.di.kr.yagoextension.repositories;

public class RepositoryFactory {

  public static Repository createDatasourceRepository(String datasource, String inputFile) {

    switch(datasource) {
      case "gadm":
        return new GADMRepository(inputFile);
      case "gag":
        return new GAGRepository(inputFile);
      case "os":
        return new OSRepository(inputFile);
      case "osi":
        return new OSIRepository(inputFile);
      case "osni":
        return new OSNIRepository(inputFile);
      default:
        throw new RuntimeException("Provided datasource is not supported");
    }
  }

  public static YAGORepository createYAGORepository(String inputFile) {

    return new YAGORepository(inputFile);
  }

  public static GADMRepository createGADMRepository(String inputFile) {

    return new GADMRepository(inputFile);
  }

  public static GAGRepository createGAGRepository(String inputFile) {

    return new GAGRepository(inputFile);
  }

  public static OSRepository createOSRepository(String inputFile) {

    return new OSRepository(inputFile);
  }

  public static OSIRepository createOSIRepository(String inputFile) {

    return new OSIRepository(inputFile);
  }

  public static OSNIRepository createOSNIRepository(String inputFile) {

    return new OSNIRepository(inputFile);
  }



}
