package io.airbrake.javabrake;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.tdunning.math.stats.Centroid;
import com.tdunning.math.stats.TDigest;

public class Tdigest {

}


class TdigestStat {
    int count = 0;
    double sum = 0;
    double sumsq = 0;
    
    transient TDigest td = TDigest.createAvlTreeDigest(10);
    String tdigest;

    public void add(long ms) {
        this.count += 1;
        this.sum += ms;
        this.sumsq += ms * ms;
        this.td.add(ms);
    }

    transient int  SMALL_ENCODING = 2;
    transient List<byte[]> list = new ArrayList<byte[]>();

    public String getData() {
    
        ByteBuffer buf = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        buf.putInt(SMALL_ENCODING);
        list.add(buf.array());
        
        buf= ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        buf.putDouble(10);
        list.add(buf.array());
        
        buf = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        buf.putInt((int)td.size());
        list.add(buf.array());
       
        
        double x = 0;
        for (Centroid centroid : td.centroids()) {
            buf = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
            double delta = centroid.mean() - x;
            x = centroid.mean();
            buf.putFloat((float) delta);
            list.add(buf.array());
        }
        
        for (Centroid centroid : td.centroids()) {
            buf = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
            int n = centroid.count();
            int k = 0;
            while (n < 0 || n > 0x7f) {
                byte b = (byte) (0x80 | (0x7f & n));
                buf.put(b);
                n = n >>> 7;
                k++;
                if (k >= 6) {
                    throw new IllegalStateException("Size is implausibly large");
                }
            }
            buf.put((byte) n);
            list.add(buf.array());
        }
       
       
        int size = 0;
        for(int i=0;i<list.size();i++)
        {
           size += list.get(i).length;      
        }

        byte[] b = new byte[size];
        int k = 0;
        for(int i=0;i<list.size();i++)
        {
            for(int j=0;j<list.get(i).length;j++)
            {
                b[k] = list.get(i)[j];
                k++;
            }
        }

         return Base64.getEncoder().encodeToString(b);

    }  

    public void encodeVarint( List<byte[]> b, int num)
    {
    while(true){
       int c = num & 0x7F;
        num >>= 7;
        if(num > 0)
        {
            b.add(new byte[] {(byte)(c | 0x80)});
        }
        else
        {
            b.add(new byte[] {(byte)(c)});
            break;
        }
    }
    }

    public byte[] serialize(TDigest tDigest) {
        byte[] bytes = new byte[tDigest.byteSize()];
        tDigest.asSmallBytes(ByteBuffer.wrap(bytes));
        return bytes;
    }
}

class TdigestStatGroup extends TdigestStat {

    Map<String, TdigestStat> groups;

    public TdigestStatGroup() {
        super();
        groups = new HashMap<>();
    }

    public void addGroups(long total_ms, Map<String, Long> group) {
        this.add(total_ms);
        for (Map.Entry<String, Long> entry : group.entrySet()) {
            this.addGroup(entry.getKey(), group.get(entry.getKey()));
        }
    }

    public void addGroup(String name, long ms) {
        TdigestStat stat = groups.get(name);
        if (stat == null) {
            stat = new TdigestStat();
            this.groups.put(name, stat);
        }
        stat.add(ms);
    }

}