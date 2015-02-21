package com.houseelectrics.serializer.googlegeocode;

import com.houseelectrics.serializer.Json2Object;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roberttodd on 25/01/2015.
 * TODO add this to c# version
 */
public class GeocodeUtil
{
    public static class PostcodeResolution
    {
        private String route;
        public String getRoute() {return route;}
        public void setRoute(String value) {this.route=value;}

        private String postalTown;
        public String getPostalTown() {return postalTown;}
        public void setPostalTown(String value) {this.postalTown = value;}

        private String locality;
        public String getLocality() {return locality;}
        public void setLocality(String value) {this.locality=value;}

        private String county;
        public String getCounty() {return county;}
        public void setCounty(String value) {this.county=value;}

        private String postalCode;
        public String getPostalCode() {return postalCode;}
        public void setPostalCode(String value) {this.postalCode=value;}

        private GeocodeResultSet.Item.Geometry geometry;
        public GeocodeResultSet.Item.Geometry getGeometry() {return geometry;}
        public void setGeometry(GeocodeResultSet.Item.Geometry value) {this.geometry=value;}

    }

    public static class GeocodeResultSet
    {
        public static class Item
        {
            public static class AddressComponents
            {
                private String long_name;
                public String getLong_name() {return long_name;}
                public void setLong_name(String value) {this.long_name=value;}
                public String short_name;
                public String getShort_name() {return short_name;}
                public void setShort_name(String value) {this.short_name=value;}

                protected List<String> types = new ArrayList<String>();
                public List<String> getTypes() {return types;}
                public void setTypes(List<String> value) {this.types=value;}

            }

            private String place_id;
            public String getPlace_id() {return place_id;}
            public void setPlace_id(String value) {this.place_id=value;}

            private List<AddressComponents> address_components=new ArrayList<AddressComponents>();
            public List<AddressComponents> getAddress_components() {return address_components;}
            public void setAddress_components(List<AddressComponents> value) {this.address_components=value;}

            private String formatted_address;
            public String getFormatted_address() {return formatted_address;}
            public void setFormatted_address(String value) {this.formatted_address = value;}

            public static class Geometry
            {
                public static class LatLong
                {
                    private double lat;
                    public double getLat() {return lat;}
                    public void setLat(double value) {this.lat=value;}
                    private double lng;
                    public double getLng() {return lng;}
                    public void setLng(double value) {this.lng=value;}
                }
                public static class Bounds
                {
                    private LatLong northeast;
                    public LatLong getNortheast() {return northeast;}
                    public void setNortheast(LatLong value) {this.northeast=value;}
                    private LatLong southwest;
                    public LatLong getSouthwest() {return southwest;}
                    public void setSouthwest(LatLong value) {this.southwest=value;}
                }
                private LatLong location;
                public LatLong getLocation() {return location;}
                public void setLocation(LatLong value) {this.location=value;}
                private String location_type;
                public String getLocation_type() {return location_type;}
                public void setLocation_type(String value) {this.location_type=value;}

                private Bounds bounds;
                public Bounds getBounds() {return bounds;}
                public void setBounds(Bounds value) {this.bounds=value;}

                private Bounds viewport;
                public Bounds getViewport() {return viewport;}
                public void setViewport(Bounds value) {this.viewport=value;}

            }

        private Geometry geometry;
        public void setGeometry(Geometry value) {this.geometry=value;}
        public Geometry getGeometry() {return geometry;}

        private List<String> types;
        public List<String> getTypes() {return types;}
        public void setTypes(List<String> value) {this.types=value;}
        }

        private List<Item> results = new ArrayList<Item>();

        public List<Item> getResults()  { return results;  }
        public void setResults(List<Item> value) {this.results=value;}

        private String status;
        public String getStatus() {return status;}
        public void setStatus(String value) {this.status=value;}

        private String error_message;
        public String getError_message() {return error_message;}
        public void setError_message(String value) {this.error_message=value;}


    }



    public interface UrlReader
    {
        public String readAllUrl(String strUrl) throws Exception;
    }


    //copied from objective c HSELUtil.
    public PostcodeResolution lookupPostcode(String postcode, UrlReader urlReader) throws Exception
    {
        postcode = postcode.replaceAll(" ", "");
        String strUrl;
        strUrl = "http://maps.googleapis.com/maps/api/geocode/json?address=" + postcode+ "&sensor=false";
        String data;
        data = urlReader.readAllUrl(strUrl);
        Json2Object json2Object = new Json2Object();
        json2Object.setPropertiesAcceptLowerCasePropertyNames(true);
        //json2Object.set
        System.out.println(data);

        GeocodeResultSet googleResult;
        googleResult = (GeocodeResultSet) json2Object.toObject(data, GeocodeResultSet.class);
        if (googleResult.getError_message()!=null && googleResult.getError_message().trim().length()>0)
        {
            throw new Exception("failed to lookup postcode error_message: " + googleResult.getError_message());
        }

        if (googleResult.getResults()==null || googleResult.getResults().size()==0) return null;
        GeocodeResultSet.Item.Geometry.LatLong location = googleResult.getResults().get(0).getGeometry().getLocation();
        strUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + location.getLat()+ "," + location.getLng()+ "&sensor=false";
        data = urlReader.readAllUrl(strUrl);
        System.out.println("*******************");
        System.out.println(data);
        googleResult = (GeocodeResultSet) json2Object.toObject(data, GeocodeResultSet.class);

        List<GeocodeResultSet.Item.AddressComponents> addressComponents =  googleResult.getResults().get(0).getAddress_components();

        PostcodeResolution result = new PostcodeResolution();
        for (int done=0; done<addressComponents.size(); done++)
        {
            GeocodeResultSet.Item.AddressComponents ac = addressComponents.get(done);
            String mainType=ac.types.get(0);

            if (mainType.equals("route"))
            {
                result.route = ac.getLong_name();
            }
            if (mainType.equals("locality"))
            {
                result.locality = ac.getLong_name();
            }
            if (mainType.equals("administrative_area_level_2"))
            {
                result.county = ac.getLong_name();
            }
            if (mainType.equals("postal_town"))
            {
                result.postalTown = ac.getLong_name();
            }
            //this is pointless !
            if (mainType.equals("postal_code"))
            {
                result.postalCode = ac.getLong_name();
            }
        }
        //result.postalCode = postcode;
        result.geometry = googleResult.getResults().get(0).getGeometry();

        return result;



   /*     NSString *url;
        NSURLRequest * urlRequest;
        NSURLResponse * response = nil;
        NSError * error = nil;
        NSData * data;
        postcode=[postcode stringByReplacingOccurrencesOfString:@" " withString:@""];
        url= [NSString stringWithFormat:@"http://maps.googleapis.com/maps/api/geocode/json?address=%@&sensor=false", postcode];

        //NSLog(@"using URL %@", url);

        urlRequest = [NSURLRequest requestWithURL:[NSURL URLWithString:url]];

        data = [NSURLConnection sendSynchronousRequest:urlRequest
        returningResponse:&response
        error:&error];

        if (error == nil)
        {
            NSDictionary *jsonObject = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingAllowFragments error:nil];

            if ([jsonObject[@"results"] count]==0)
            {
                return nil;
            }
            id location = jsonObject[@"results"][0][@"geometry"][@"location"];
            NSNumber *lat = location[@"lat"];
            NSNumber *lng = location[@"lng"];

            url= [NSString stringWithFormat:@"http://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false", lat.doubleValue, lng.doubleValue];

            urlRequest = [NSURLRequest requestWithURL:[NSURL URLWithString:url]];

            data = [NSURLConnection sendSynchronousRequest:urlRequest
            returningResponse:&response
            error:&error];
            if (error == nil)
            {
                jsonObject = [NSJSONSerialization JSONObjectWithData:data options:0 error:nil];
                NSArray * addressComponents = jsonObject[@"results"][0][@"address_components"];
                HSELPostcodeResolution *result = [[HSELPostcodeResolution alloc]init];

                for (int done=0; done<addressComponents.count; done++)
                {
                    NSDictionary *ac = addressComponents[done];
                    NSString *mainType = ac[@"types"][0];

                    NSLog(@"%@ %@",ac[@"long_name"], mainType);
                    if ([mainType isEqualToString:@"route"])
                    {
                        result.route = ac[@"long_name"];
                    }
                    if ([mainType isEqualToString:@"locality"])
                    {
                        result.locality = ac[@"long_name"];
                    }
                    if ([mainType isEqualToString:@"administrative_area_level_2"])
                    {
                        result.county = ac[@"long_name"];
                    }
                    if ([mainType isEqualToString:@"postal_town"])
                    {
                        result.postalTown = ac[@"long_name"];
                    }
                    if ([mainType isEqualToString:@"postal_code"])
                    {
                        result.postalCode = ac[@"long_name"];
                    }
                }
                result.postalCode = postcode;
                return result;
            }
        }*/

    }

 /*   public String readJSONFeed(String URL) {
        StringBuilder stringBuilder = new StringBuilder();
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(URL);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                inputStream.close();
            } else {
                Log.d("JSON", "Failed to download file");
            }
        } catch (Exception e) {
            Log.d("readJSONFeed", e.getLocalizedMessage());
        }
        return stringBuilder.toString();
    }
*/


}
