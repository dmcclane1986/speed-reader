package com.speedreader.trainer.di;

import com.speedreader.trainer.data.remote.OpenAIService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AppModule_ProvideOpenAIServiceFactory implements Factory<OpenAIService> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  public AppModule_ProvideOpenAIServiceFactory(Provider<OkHttpClient> okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public OpenAIService get() {
    return provideOpenAIService(okHttpClientProvider.get());
  }

  public static AppModule_ProvideOpenAIServiceFactory create(
      Provider<OkHttpClient> okHttpClientProvider) {
    return new AppModule_ProvideOpenAIServiceFactory(okHttpClientProvider);
  }

  public static OpenAIService provideOpenAIService(OkHttpClient okHttpClient) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideOpenAIService(okHttpClient));
  }
}
