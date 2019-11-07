import java.io.Serializable;

public class Job implements Serializable {
    private int number;
    private String fileName;

    public Job(int number, String fileName) {
        this.number = number;
        this.fileName = fileName;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String toString() {
        return "job{" +
                "number=" + number +
                ", fileName='" + fileName + '\'' +
                '}';
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Job job = (Job) obj;
        return job.getNumber() == this.number;
    }
}