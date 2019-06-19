package io.wookey.monero.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SubaddressRow implements Parcelable, Comparable<SubaddressRow> {
    public int rowId;
    public String address;
    public String label;

    public SubaddressRow(int rowId, String address, String label) {
        this.rowId = rowId;
        this.address = address;
        this.label = label;
    }

    public String toString() {
        return "#" + rowId + " " + label + " " + address;
    }

    public static final Creator<SubaddressRow> CREATOR = new Creator<SubaddressRow>() {
        public SubaddressRow createFromParcel(Parcel in) {
            return new SubaddressRow(in);
        }

        public SubaddressRow[] newArray(int size) {
            return new SubaddressRow[size];
        }
    };

    private SubaddressRow(Parcel in) {
        rowId = in.readInt();
        address = in.readString();
        label = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(rowId);
        dest.writeString(address);
        dest.writeString(label);
    }

    @Override
    public int compareTo(SubaddressRow o) {
        return o.rowId - this.rowId;
    }
}
