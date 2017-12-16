package backend;

/** Error class that is raised when a tag contains forbidden symbols. */
public class InvalidTagException extends RuntimeException {
    public InvalidTagException(String message){
        super(message);
    }
}
