package ${escapeKotlinIdentifiers(packageName)}

<#if adFormat == "banner">
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
<#elseif adFormat == "interstitial">
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
</#if>

import android.os.Bundle
import ${superClassFqcn}
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
<#if applicationPackage??>
import ${applicationPackage}.R
</#if>

import kotlinx.android.synthetic.main.${layoutName}.*

// Remove the line below after defining your own ad unit ID.
private const val TOAST_TEXT = "Test ads are being shown. " +
        "To show live ads, replace the ad unit ID in res/values/strings.xml " +
        "with your own ad unit ID."
private const val START_LEVEL = 1

class ${activityClass} : ${superClass}() {

<#if adFormat == "interstitial">
    private var currentLevel: Int = 0
    private var interstitialAd: InterstitialAd? = null
</#if>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.${layoutName})

<#if adFormat == "banner">
        // Load an ad into the AdMob banner view.
        val adRequest = AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template")
                .build()
        adView.loadAd(adRequest)
<#elseif adFormat == "interstitial">
        // Create the next level button, which tries to show an interstitial when clicked.
        next_level_button.isEnabled = false
        next_level_button.setOnClickListener { showInterstitial() }

        // Create the text view to show the level number.
        currentLevel = START_LEVEL

        // Create the InterstitialAd and set the adUnitId (defined in values/strings.xml).
        interstitialAd = newInterstitialAd()
        loadInterstitial()
</#if>

        // Toasts the test ad message on the screen. Remove this after defining your own ad unit ID.
        Toast.makeText(this, TOAST_TEXT, Toast.LENGTH_LONG).show()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.${menuName}, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                R.id.action_settings -> true
                else -> super.onOptionsItemSelected(item)
            }

<#if adFormat == "interstitial">
    private fun newInterstitialAd(): InterstitialAd {
        return InterstitialAd(this).apply {
            adUnitId = getString(R.string.interstitial_ad_unit_id)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    next_level_button.isEnabled = true
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    next_level_button.isEnabled = true
                }

                override fun onAdClosed() {
                    // Proceed to the next level.
                    goToNextLevel()
                }
            }
        }
    }

    private fun showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and reload the ad.
        if (interstitialAd?.isLoaded == true) {
            interstitialAd?.show()
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show()
            goToNextLevel()
        }
    }

    private fun loadInterstitial() {
        // Disable the next level button and load the ad.
        next_level_button.isEnabled = false
        val adRequest = AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template")
                .build()
        interstitialAd?.loadAd(adRequest)
    }

    private fun goToNextLevel() {
        // Show the next level and reload the ad to prepare for the level after.
        level.text = "Level " + (++currentLevel)
        interstitialAd = newInterstitialAd()
        loadInterstitial()
    }
</#if>
}
