package net.minecraft.server.management;

import com.google.gson.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class BanEntry<T> extends UserListEntry<T>
{
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    protected final Date banStartDate;
    protected final String bannedBy;
    protected final Date banEndDate;
    protected final String reason;

    public BanEntry(T valueIn, Date startDate, String banner, Date endDate, String banReason)
    {
        super(valueIn);
        banStartDate = startDate == null ? new Date() : startDate;
        bannedBy = banner == null ? "(Unknown)" : banner;
        banEndDate = endDate;
        reason = banReason == null ? "Banned by an operator." : banReason;
    }

    protected BanEntry(T p_i1174_1_, JsonObject p_i1174_2_)
    {
        super(p_i1174_1_, p_i1174_2_);
        Date date;

        try
        {
            date = p_i1174_2_.has("created") ? BanEntry.dateFormat.parse(p_i1174_2_.get("created").getAsString()) : new Date();
        }
        catch (ParseException var7)
        {
            date = new Date();
        }

        banStartDate = date;
        bannedBy = p_i1174_2_.has("source") ? p_i1174_2_.get("source").getAsString() : "(Unknown)";
        Date date1;

        try
        {
            date1 = p_i1174_2_.has("expires") ? BanEntry.dateFormat.parse(p_i1174_2_.get("expires").getAsString()) : null;
        }
        catch (ParseException var6)
        {
            date1 = null;
        }

        banEndDate = date1;
        reason = p_i1174_2_.has("reason") ? p_i1174_2_.get("reason").getAsString() : "Banned by an operator.";
    }

    public Date getBanEndDate()
    {
        return banEndDate;
    }

    public String getBanReason()
    {
        return reason;
    }

    boolean hasBanExpired()
    {
        return banEndDate != null && banEndDate.before(new Date());
    }

    protected void onSerialization(JsonObject data)
    {
        data.addProperty("created", BanEntry.dateFormat.format(banStartDate));
        data.addProperty("source", bannedBy);
        data.addProperty("expires", banEndDate == null ? "forever" : BanEntry.dateFormat.format(banEndDate));
        data.addProperty("reason", reason);
    }
}
