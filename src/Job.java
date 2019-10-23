public class Job implements JobInterface {
    private int number;
    private String fileName;

    public Job(int number, String fileName) {
        this.number = number;
        this.fileName = fileName;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "job{" +
                "number=" + number +
                ", fileName='" + fileName + '\'' +
                '}';
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        JobInterface jobInterface = (JobInterface) obj;
        return jobInterface.getNumber() == this.number;
    }
}