public interface JobInterface {
    int getNumber();

    void setNumber(int number);

    String getFileName();

    void setFileName(String fileName);

    @Override
    String toString();

    @Override
    boolean equals(Object obj);
}
