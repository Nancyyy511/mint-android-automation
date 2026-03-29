package api.models;

public class LoginRequest {
    private GeoLocation geoLocation;
    private String email;
    private String password;
    private int useBiometric;

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUseBiometric() {
        return useBiometric;
    }

    public void setUseBiometric(int useBiometric) {
        this.useBiometric = useBiometric;
    }
}
