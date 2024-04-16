package com.example.kinnectify.auth.presentation.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kinnectify.R
import com.example.kinnectify.auth.presentation.AuthViewModel
import com.example.kinnectify.common.helper.MyValidation
import com.example.kinnectify.databinding.FragmentLoginBinding
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.kinnectify.common.ProgressDialogUtil
import com.example.kinnectify.common.utils.UIState
import com.example.kinnectify.models.User
import com.example.kinnectify.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

//    private val viewModel by viewModels<ViewModelSignUser>()

    private val viewModel by viewModels<AuthViewModel>()

    private val progressDialogUtil =  ProgressDialogUtil()




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }






    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.signInUserState.collect { state ->
                handleState(state)
            }
        }



        binding.loginBtnLogIn.setOnClickListener {
            val email: String = binding.inputTextLayoutEmail.editText!!.text.toString()
            val password: String = binding.inputTextLayoutPassword.editText!!.text.toString()

            if (MyValidation.isValidEmail(requireContext(), binding.inputTextLayoutEmail)
                && MyValidation.validatePass(requireContext(), binding.inputTextLayoutPassword)
            ) {
                viewModel.signInWithEmailAndPassword(email, password)
            }
        }



        binding.loginBtnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.loginForget.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_resetPasswordFragment)
        }
    }


    private fun handleState(state: UIState<User>) {

        when (state) {
            UIState.Empty -> {}
            is UIState.Error -> {
                progressDialogUtil.hideProgressDialog()

                Toast.makeText(context, "" + state.error, Toast.LENGTH_SHORT).show()
            }

            UIState.Loading -> {
                progressDialogUtil.showProgressDialog(requireActivity())
            }
            is UIState.Success -> {
                progressDialogUtil.hideProgressDialog()

                activity?.startActivity(Intent(activity, MainActivity::class.java))
                activity?.finish()


            }
        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}