package com.jams.calculadora

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.mariuszgromada.math.mxparser.Expression

class MainActivity : AppCompatActivity() {

    private lateinit var display: EditText

    // Botões de controle e especiais
    private lateinit var btnponto: Button
    private lateinit var btndel: Button
    private lateinit var btnresultado: Button
    private lateinit var btnlimpar: Button

    // Operadores
    private lateinit var btnsomar: Button
    private lateinit var btnsubtrair: Button
    private lateinit var btnmultiplicar: Button
    private lateinit var btndividir: Button
    private lateinit var btnporcento: Button
    private lateinit var btnexp: Button

    // Números
    private lateinit var btn0: Button
    private lateinit var btn1: Button
    private lateinit var btn2: Button
    private lateinit var btn3: Button
    private lateinit var btn4: Button
    private lateinit var btn5: Button
    private lateinit var btn6: Button
    private lateinit var btn7: Button
    private lateinit var btn8: Button
    private lateinit var btn9: Button

    // Listas para facilitar a configuração
    private lateinit var allBtnNumbers: List<Button>
    private lateinit var allBtnOperators: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        iniciarListeners()
    }

    private fun initializeViews() {
        display = findViewById(R.id.display)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            display.showSoftInputOnFocus = false
        } else {
            display.inputType = android.text.InputType.TYPE_NULL
        }

        btnponto = findViewById(R.id.btnponto)
        btndel = findViewById(R.id.btndel)
        btnlimpar = findViewById(R.id.btnlimpar)
        btnresultado = findViewById(R.id.btnresultado)
        btnsomar = findViewById(R.id.btnsomar)
        btnsubtrair = findViewById(R.id.btnsubtrair)
        btnmultiplicar = findViewById(R.id.btnmultiplicar)
        btndividir = findViewById(R.id.btndividir)
        btnporcento = findViewById(R.id.btnporcento)
        btnexp = findViewById(R.id.btnexp)

        btn0 = findViewById(R.id.btn0)
        btn1 = findViewById(R.id.btn1)
        btn2 = findViewById(R.id.btn2)
        btn3 = findViewById(R.id.btn3)
        btn4 = findViewById(R.id.btn4)
        btn5 = findViewById(R.id.btn5)
        btn6 = findViewById(R.id.btn6)
        btn7 = findViewById(R.id.btn7)
        btn8 = findViewById(R.id.btn8)
        btn9 = findViewById(R.id.btn9)

        allBtnNumbers = listOf(btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9)
        allBtnOperators = listOf(btnsomar, btnsubtrair, btnmultiplicar, btndividir, btnporcento, btnexp)
    }

    private fun iniciarListeners() {
        // Listener para os botões numéricos
        allBtnNumbers.forEach { button ->
            button.setOnClickListener { inserirNumero(button.text.toString()) }
        }

        // Listener para os botões de operadores
        allBtnOperators.forEach { button ->
            button.setOnClickListener { inserirOperador(button.text.toString()) }
        }

        // Listeners para botões com lógica específica
        btnlimpar.setOnClickListener {
            display.setText("0")
            display.setSelection(1)
        }

        btndel.setOnClickListener {
            delUltimo()
        }

        btnponto.setOnClickListener {
            inserirVirgula()
        }

        btnresultado.setOnClickListener {
            calcular()
        }
    }

    private fun inserirNumero(number: String) {
        val currentText = display.text.toString()
        val cursorPosition = display.selectionStart

        if (currentText == "0" || currentText == "Erro") {
            display.setText(number)
            display.setSelection(1)
        } else {
            display.text.insert(cursorPosition, number)
        }
    }

    private fun inserirOperador(operator: String) {
        val currentText = display.text.toString()
        if (currentText.isEmpty()) return

        val lastChar = currentText.last()

        // Se o último caractere já for um operador, substitui pelo novo
        if (lastChar.isOperator()) {
            val textWithoutLast = currentText.dropLast(1)
            display.setText(textWithoutLast + operator)
        } else {
            display.append(operator)
        }
        display.setSelection(display.text.length)
    }

    private fun inserirVirgula() {
        val currentText = display.text.toString()
        // Encontra o último operador para verificar apenas o número atual
        val lastOperatorIndex = currentText.findLastAnyOf(listOf("+", "-", "x", "÷", "%", "^"))?.first ?: -1
        val currentNumber = currentText.substring(lastOperatorIndex + 1)

        // Adiciona a vírgula apenas se o número atual ainda não tiver uma
        if (!currentNumber.contains(",")) {
            display.text.insert(display.selectionStart, ",")
        }
    }

    private fun delUltimo() {
        val currentText = display.text.toString()
        val cursorPosition = display.selectionStart

        if (cursorPosition > 0) {
            val newText = currentText.substring(0, cursorPosition - 1) + currentText.substring(cursorPosition)
            if (newText.isEmpty()) {
                display.setText("0")
                display.setSelection(1)
            } else {
                display.setText(newText)
                display.setSelection(cursorPosition - 1)
            }
        }
    }

    private fun calcular() {
        var expressionText = display.text.toString()

        // A biblioteca mxparser usa padrões matemáticos.
        expressionText = expressionText.replace('x', '*')
        expressionText = expressionText.replace('÷', '/')
        expressionText = expressionText.replace(',', '.')

        // Verifica se a expressão contém o operador de porcentagem
        if (expressionText.contains("%")) {
            // Expressão regular (Regex) para encontrar o padrão "número % número"
            // Ex: "50%200" ou "10+50%200"
            val pattern = "(\\d*\\.?\\d+)%(\\d*\\.?\\d+)".toRegex()
            expressionText = pattern.replace(expressionText) { matchResult ->
                // Pega o número antes do % (grupo 1) e o número depois (grupo 2)
                val base = matchResult.groupValues[1]
                val value = matchResult.groupValues[2]
                // Substitui "base%value" por "((base/100)*value)"
                "((${base}/100)*${value})"
            }
        }

        // Remove o último caractere se for um operador (após qualquer substituição)
        if (expressionText.lastOrNull()?.isOperator() == true) {
            expressionText = expressionText.dropLast(1)
        }

        val expression = Expression(expressionText)
        val result = expression.calculate()

        // Se o resultado for NaN (Not a Number), significa que a expressão é inválida
        if (result.isNaN()) {
            display.setText("Erro")
        } else {
            // Formata o resultado para remover o ".0" de números inteiros
            val formattedResult = if (result % 1 == 0.0) {
                result.toLong().toString()
            } else {
                // Converte de volta para vírgula para exibição
                result.toString().replace('.', ',')
            }
            display.setText(formattedResult)
        }
        display.setSelection(display.text.length)
    }

    // Função de extensão para verificar se um caractere é um operador
    private fun Char.isOperator(): Boolean {
        return this in listOf('+', '-', 'x', '÷', '%', '^')
    }
}