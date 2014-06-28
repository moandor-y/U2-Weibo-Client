package gov.moandor.androidweibo.adapter;

public interface ISelectableAdapter<T> {
    public void setSelectedPosition(int position);

    public T getSelectedItem();

    public int getSelection();
}
