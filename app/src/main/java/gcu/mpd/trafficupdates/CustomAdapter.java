package gcu.mpd.trafficupdates;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/*////////////////////////////
                           //
Student: Paul James Kerr   //
Matric no: S1828425        //
                           //
/////////////////////////// */

public class CustomAdapter extends BaseAdapter implements Filterable
{

    private List<RssResponse>originalData = null;
    private List<RssResponse>filteredData = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();

    public CustomAdapter(Context context, List<RssResponse> data) {
        this.filteredData = data ;
        this.originalData = data ;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return filteredData.size();
    }

    public Object getItem(int position) {
        return filteredData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public List<RssResponse> getFilteredData() {
        return filteredData;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);

            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(android.R.id.text1);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(filteredData.get(position).getTitle());
        return convertView;
    }

    static class ViewHolder {
        TextView text;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<RssResponse> list = originalData;

            int count = list.size();
            final ArrayList<RssResponse> nlist = new ArrayList<RssResponse>(count);

            RssResponse filterableResponse;

            for (int i = 0; i < count; i++) {
                filterableResponse = list.get(i);
                if (filterableResponse.getTitle().toLowerCase().contains(filterString)) {
                    RssResponse filtered = new RssResponse();
                    filtered.setTitle(filterableResponse.getTitle());
                    filtered.setUniqueID(filterableResponse.getUniqueID());
                    filtered.setIsDefault(filterableResponse.getIsDefault());
                    filtered.setStartDate(filterableResponse.getStartDate());
                    filtered.setEndDate(filterableResponse.getEndDate());
                    filtered.setDescription(filterableResponse.getDescription());
                    filtered.setLink(filterableResponse.getLink());
                    filtered.setCoords(filterableResponse.getCoords());
                    filtered.setPublished(filterableResponse.getPublished());
                    nlist.add(filtered);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<RssResponse>) results.values;
            notifyDataSetChanged();
        }

    }
}