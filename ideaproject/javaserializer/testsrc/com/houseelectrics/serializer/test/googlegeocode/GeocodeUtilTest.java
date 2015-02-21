package com.houseelectrics.serializer.test.googlegeocode;
import com.houseelectrics.serializer.googlegeocode.GeocodeUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by roberttodd on 25/01/2015.
 */
public class GeocodeUtilTest
{

    @Test
    public void postcodeLookupTest() throws Exception
    {
        GeocodeUtil resolver =new GeocodeUtil();
        GeocodeUtil.PostcodeResolution result;
        String postcode = "SW1A 2AA";

        GeocodeUtil.UrlReader urlReader = new GeocodeUtil.UrlReader()
        {
            @Override
            public String readAllUrl(String strUrl) throws Exception
            {
                URL oracle = new URL(strUrl);
                StringBuffer sb = new StringBuffer();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(oracle.openStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    sb.append(inputLine);
                    sb.append("\r\n");
                in.close();
                return sb.toString();
            }
        };


        result =resolver.lookupPostcode(postcode, urlReader);
        GeocodeUtil.GeocodeResultSet.Item.Geometry geometry = result.getGeometry();
        double northLessLat = geometry.getViewport().getNortheast().getLat()-geometry.getLocation().getLat();
        double southLessLat = geometry.getViewport().getSouthwest().getLat()-geometry.getLocation().getLat();
        double eastLessLng = geometry.getViewport().getNortheast().getLng()-geometry.getLocation().getLng();
        double westLessLng = geometry.getViewport().getSouthwest().getLng()-geometry.getLocation().getLng();

        Assert.assertNotNull("postcode should be found " + postcode , result);
        Assert.assertEquals("expected PMs address", result.getRoute(), "Downing Street");
        Assert.assertNotNull(result.getGeometry());

        postcode = "YYYYYYYYYYY";
        result =resolver.lookupPostcode(postcode, urlReader);
        Assert.assertNull("postcode should not be found " + postcode, result);
    }


}
