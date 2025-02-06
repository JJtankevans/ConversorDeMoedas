package com.vitoroliveira.conversordemoedas.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.vitoroliveira.conversordemoedas.R
import com.vitoroliveira.conversordemoedas.databinding.ItemCurrencyTypeBinding
import com.vitoroliveira.conversordemoedas.network.model.CurrencyType

class CurrencyTypesAdapter(
    private val currencyTypes: List<CurrencyType>
): BaseAdapter() {
    override fun getCount(): Int {
        return currencyTypes.size
    }

    override fun getItem(itemPosition: Int): Any {
        return currencyTypes[itemPosition]
    }

    override fun getItemId(position: Int): Long {
        return currencyTypes[position].hashCode().toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return convertView ?: run {
            val item = currencyTypes[position]
            val binding = ItemCurrencyTypeBinding.inflate(LayoutInflater.from(parent?.context))
            with(binding) {
                tvCurrencyAcronym.text = item.acronym
                ivFlag.setImageResource(R.drawable.img_br)
            }

            binding.root
        }
    }
}