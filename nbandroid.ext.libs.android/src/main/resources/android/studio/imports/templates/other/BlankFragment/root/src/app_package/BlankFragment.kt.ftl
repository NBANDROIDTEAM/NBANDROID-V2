package ${escapeKotlinIdentifiers(packageName)}

<#if includeCallbacks>import android.content.Context
import android.net.Uri</#if>
import android.os.Bundle
import ${getMaterialComponentName('android${SupportPackage}.app.Fragment', useAndroidX)}
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
<#if !includeLayout>import android.widget.TextView</#if>
<#if applicationPackage??>
import ${applicationPackage}.R
</#if>

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 <#if includeCallbacks>
 * Activities that contain this fragment must implement the
 * [${className}.OnFragmentInteractionListener] interface
 * to handle interaction events.
 </#if>
 <#if includeFactory>
 * Use the [${className}.newInstance] factory method to
 * create an instance of this fragment.
 </#if>
 *
 */
class ${className} : Fragment() {
<#if includeFactory>
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
</#if>
<#if includeCallbacks>
    private var listener: OnFragmentInteractionListener? = null
</#if>
<#if includeFactory>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
</#if>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
<#if includeLayout>
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.${fragmentName}, container, false)
<#else>
        return TextView(activity).apply {
            setText(R.string.hello_blank_fragment)
        }
</#if>
    }

<#if includeCallbacks>
    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
            listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
</#if>

<#if includeFactory>
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ${className}.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic fun newInstance(param1: String, param2: String) =
                ${className}().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
</#if>
}
