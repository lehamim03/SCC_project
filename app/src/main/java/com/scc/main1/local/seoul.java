package com.scc.main1.local;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import com.scc.main1.Getevent;

public class seoul {
    public static void SeoulData(final String selectedMonth, final DataCallback callback) {
        final List<EventData> eventList = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = database.getReference();

        DatabaseReference childRef = rootRef.child(selectedMonth);

        eventList.clear();

        childRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        EventData eventData = childSnapshot.getValue(EventData.class);
                        eventList.add(eventData);
                    }
                    callback.onDataLoaded(eventList);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }

    public interface DataCallback {
        void onDataLoaded(List<EventData> eventList);
    }
}
