package com.vitoroliveira.conversordemoedas

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.vitoroliveira.conversordemoedas.databinding.ActivityMainBinding
import com.vitoroliveira.conversordemoedas.network.model.CurrencyType
import com.vitoroliveira.conversordemoedas.ui.CurrencyTypesAdapter
import com.vitoroliveira.conversordemoedas.viewmodel.CurrencyExchangeViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<CurrencyExchangeViewModel>()

    private var exchangeRate: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /* Chama a funcao que vai buscar os tipos de moedas no viewModel
        * e CASO tenha sucesso ao buscar as moedas é feita a atualizaçao
        * do estado */
        viewModel.requireCurrencyTypes()
        binding.etFromExchangeValue.addCurrencyMask()

        /**/
        lifecycleScope.apply {
            launch {
                /* Observa o estado das moedas e caso tenha atualizaçao
                * chama a funcao de extensao passando a nova listagem de moedas */
                viewModel.currencyTypes.collect { result ->
                    result.onSuccess { currencyTypesList ->
                        binding.configureCurrencyTypeSpinners(currencyTypes = currencyTypesList)
                    }.onFailure {
                        Toast.makeText(
                            this@MainActivity,
                            it.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            launch {
                viewModel.exchangeRate.collect { result ->
                    result.onSuccess { currencyExchangeData ->
                        currencyExchangeData?.let {
                            exchangeRate = currencyExchangeData.exchangeRate
                            binding.generateConvertedValue()
                        }
                    }.onFailure {
                        Toast.makeText(
                            this@MainActivity,
                            it.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    /* Funcao de extensao para ter acesso aos ids dos spinners */
    private fun ActivityMainBinding.configureCurrencyTypeSpinners(currencyTypes: List<CurrencyType>) {
        spnFromExchange.apply {
            //Atualiza a lista de moedas recebidas
            adapter = CurrencyTypesAdapter(currencyTypes)
            //Seta as acoes que precisam acontecer ao se elecionar uma nova moeda no spinner
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val from = currencyTypes[position]
                    val to = currencyTypes[spnToExchange.selectedItemPosition]

                    tvFromCurrencySymbol.text = from.symbol

                    viewModel.requireExchangeRate(
                        from = from.acronym,
                        to = to.acronym
                    )
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}

            }
        }
        spnToExchange.apply {
            adapter = CurrencyTypesAdapter(currencyTypes)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val from = currencyTypes[spnFromExchange.selectedItemPosition]
                    val to = currencyTypes[position]

                    tvToCurrencySymbol.text = to.symbol

                    viewModel.requireExchangeRate(
                        from = from.acronym,
                        to = to.acronym
                    )
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    currencyTypes.firstOrNull()?.let { firstCurrencyType ->

                        tvToCurrencySymbol.text = firstCurrencyType.symbol
                        tvFromCurrencySymbol.text = firstCurrencyType.symbol

                        viewModel.requireExchangeRate(
                            from = firstCurrencyType.acronym,
                            to = firstCurrencyType.acronym
                        )
                    }
                }

            }
        }
    }

    private fun ActivityMainBinding.generateConvertedValue() {
        exchangeRate?.let {
            val cleanedString = etFromExchangeValue.text.toString().replace("[,.]".toRegex(), "")
            val currencyValue = cleanedString.toDoubleOrNull() ?: 0.0


            val formattedValue = DecimalFormat(
                "#,##0.00",
                DecimalFormatSymbols(Locale.getDefault())
            ).format((currencyValue * it / 100))

            tvToExchangeValue.text = formattedValue
        }
    }

    private fun EditText.addCurrencyMask() {
        addTextChangedListener(object : TextWatcher {
            private var currentText = ""
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != currentText) {
                    removeTextChangedListener(this)

                    val cleanedString = s.toString().replace("[,.]".toRegex(), "")
                    val currencyValue = cleanedString.toDoubleOrNull() ?: 0.0

                    val formattedValue = DecimalFormat(
                        "#,##0.00",
                        DecimalFormatSymbols(Locale.getDefault())
                    ).format((currencyValue / 100))

                    currentText = formattedValue
                    setText(formattedValue)
                    setSelection(formattedValue.length)

                    binding.generateConvertedValue()

                    addTextChangedListener(this)
                }
            }
        })
    }
}