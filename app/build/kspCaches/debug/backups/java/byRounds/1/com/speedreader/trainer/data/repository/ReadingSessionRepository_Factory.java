package com.speedreader.trainer.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.speedreader.trainer.data.remote.OpenAIService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class ReadingSessionRepository_Factory implements Factory<ReadingSessionRepository> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<FirebaseAuth> firebaseAuthProvider;

  private final Provider<OpenAIService> openAIServiceProvider;

  public ReadingSessionRepository_Factory(Provider<FirebaseFirestore> firestoreProvider,
      Provider<FirebaseAuth> firebaseAuthProvider, Provider<OpenAIService> openAIServiceProvider) {
    this.firestoreProvider = firestoreProvider;
    this.firebaseAuthProvider = firebaseAuthProvider;
    this.openAIServiceProvider = openAIServiceProvider;
  }

  @Override
  public ReadingSessionRepository get() {
    return newInstance(firestoreProvider.get(), firebaseAuthProvider.get(), openAIServiceProvider.get());
  }

  public static ReadingSessionRepository_Factory create(
      Provider<FirebaseFirestore> firestoreProvider, Provider<FirebaseAuth> firebaseAuthProvider,
      Provider<OpenAIService> openAIServiceProvider) {
    return new ReadingSessionRepository_Factory(firestoreProvider, firebaseAuthProvider, openAIServiceProvider);
  }

  public static ReadingSessionRepository newInstance(FirebaseFirestore firestore,
      FirebaseAuth firebaseAuth, OpenAIService openAIService) {
    return new ReadingSessionRepository(firestore, firebaseAuth, openAIService);
  }
}
