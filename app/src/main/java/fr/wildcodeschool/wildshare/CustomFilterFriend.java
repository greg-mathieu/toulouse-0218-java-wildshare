package fr.wildcodeschool.wildshare;

import android.widget.Filter;

import java.util.ArrayList;

/**
 * Created by wilder on 11/04/18.
 */

public class CustomFilterFriend extends Filter {

    private ArrayList<FriendModel> filterList;
    private FriendListAdapter friendListAdapter;

    public CustomFilterFriend(ArrayList<FriendModel> filterList, FriendListAdapter friendListAdapter) {

        this.filterList = filterList;
        this.friendListAdapter = friendListAdapter;
    }

    /** Création de la searchview qui va nous permettre de chercher un monstre par son nom
     * La searchview créait une nouvelle liste de monstres en les filtrant
     */
    public ArrayList<FriendModel> filteredFriend;
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();
            filteredFriend = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                if (filterList.get(i).getFirstname().toUpperCase().contains(constraint)
                        ||
                    filterList.get(i).getLastname().toUpperCase().contains(constraint)) {

                    filteredFriend.add(filterList.get(i));
                }
            }

            results.count = filteredFriend.size();
            results.values = filteredFriend;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        friendListAdapter.friendModel = (ArrayList<FriendModel>) results.values;
        friendListAdapter.notifyDataSetChanged();
    }
}